"""
Plugin: Scraper do Mercado Livre (Porta Secundária)
Responsabilidade: Buscar preços do Volante Logitech G29 no Mercado Livre
utilizando Playwright e salvar/atualizar o histórico em JSON.
"""

import asyncio
import json
import os
import re
from datetime import datetime
from playwright.async_api import async_playwright, TimeoutError as PlaywrightTimeoutError

# ─────────────────────────────────────────────
# Configurações
# ─────────────────────────────────────────────
SEARCH_TERM = "Volante Logitech G29"
BASE_URL = "https://www.mercadolivre.com.br"
# URL directa da listagem - evita redirecionamentos do /s?as_word=
SEARCH_URL = "https://lista.mercadolivre.com.br/volante-logitech-g29"
OUTPUT_JSON = "mercadolivre_g29_prices.json"

# Palavras que devem aparecer no título (todas obrigatórias)
REQUIRED_KEYWORDS = ["logitech", "g29"]

# Palavras proibidas no título (acessórios indesejados)
BANNED_KEYWORDS = [
    "cambio", "suporte", "adaptador", "placa",
    "pedal avulso", "fonte", "capa", "presilha", "cabo",
    "substituicao", "reparo", "peca", "cirurgia",
    "freio de mao", "freio de m"
]

MAX_RESULTS = 15   # quantos anúncios capturar por execução


# ─────────────────────────────────────────────
# Utilitários
# ─────────────────────────────────────────────

def clean_price(price_str: str) -> float:
    """Converte string de preço brasileiro (ex: R$ 3.299,90) para float."""
    if not price_str:
        return float("inf")
    clean = re.sub(r"[^\d,.]", "", price_str)
    if not clean:
        return float("inf")
    # Formato brasileiro: 3.299,90 → 3299.90
    if "," in clean and "." in clean:
        clean = clean.replace(".", "").replace(",", ".")
    elif "," in clean:
        clean = clean.replace(",", ".")
    try:
        return float(clean)
    except ValueError:
        return float("inf")


def format_brl(value: float) -> str:
    """Formata float como moeda brasileira (R$ 3.299,90)."""
    return f"R$ {value:,.2f}".replace(",", "v").replace(".", ",").replace("v", ".")


def is_valid_item(title: str, price: float) -> bool:
    """Filtra itens usando palavras-chave obrigatórias e banidas."""
    title_lower = title.lower()

    # Rejeita acessórios banidos
    if any(b in title_lower for b in BANNED_KEYWORDS):
        return False

    # Exige todas as palavras-chave
    if not all(k in title_lower for k in REQUIRED_KEYWORDS):
        return False

    # Filtro de sanidade: G29 custa acima de R$ 800
    if price < 800:
        return False

    return True


# ─────────────────────────────────────────────
# Scraper (Playwright Assíncrono)
# ─────────────────────────────────────────────

async def scrape_g29_prices() -> list[dict]:
    """
    Acessa o Mercado Livre, pesquisa pelo G29 e extrai os anúncios válidos.
    Retorna lista de dicts: {title, price_text, price_value, url, image, seller}.
    """
    results = []

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context(
            locale="pt-BR",
            user_agent=(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/124.0.0.0 Safari/537.36"
            ),
            viewport={"width": 1366, "height": 768},
        )
        page = await context.new_page()

        print(f"Acessando: {SEARCH_URL}")

        try:
            await page.goto(SEARCH_URL, wait_until="domcontentloaded", timeout=30000)
        except PlaywrightTimeoutError:
            print("Timeout ao carregar a pagina de busca.")
            await browser.close()
            return results

        # Aguarda os cards Polycard do Mercado Livre
        try:
            await page.wait_for_selector(".poly-card", timeout=15000)
        except PlaywrightTimeoutError:
            # Fallback: aguarda qualquer elemento de produto
            try:
                await page.wait_for_selector("[class*='ui-search']", timeout=8000)
            except PlaywrightTimeoutError:
                print("Nenhum resultado encontrado na pagina.")
                await browser.close()
                return results

        # Extrai dados via evaluate usando os seletores do sistema Polycard do ML
        raw_items = await page.evaluate("""() => {
            // Polycard (design system atual do Mercado Livre)
            const cards = document.querySelectorAll('.poly-card');
            const items = [];

            cards.forEach(card => {
                const titleEl  = card.querySelector('a.poly-component__title');
                const priceEl  = card.querySelector(
                    '.poly-price__current .andes-money-amount__fraction'
                );
                const centsEl  = card.querySelector(
                    '.poly-price__current .andes-money-amount__cents'
                );
                const imgEl    = card.querySelector('img');
                const sellerEl = card.querySelector('span.poly-component__seller');

                if (!titleEl || !priceEl) return;

                const fraction = priceEl.innerText.trim().replace(/[.]/g, '');
                const cents    = centsEl ? centsEl.innerText.trim().padStart(2, '0') : '00';
                const priceStr = fraction + ',' + cents;

                items.push({
                    title:  titleEl.innerText.trim(),
                    price:  priceStr,
                    url:    titleEl.href || '',
                    image:  imgEl  ? imgEl.src  : '',
                    seller: sellerEl ? sellerEl.innerText.trim() : 'N/A'
                });
            });
            return items;
        }""")

        await browser.close()

        # Filtra e formata
        for item in raw_items[:MAX_RESULTS * 3]:  # analisa mais, filtra depois
            price_val = clean_price(item["price"])
            if is_valid_item(item["title"], price_val):
                results.append({
                    "title":       item["title"],
                    "price_text":  f"R$ {item['price']}",
                    "price_value": price_val,
                    "url":         item["url"],
                    "image":       item["image"],
                    "seller":      item["seller"],
                })
            if len(results) >= MAX_RESULTS:
                break

    return results


# ─────────────────────────────────────────────
# Persistência em JSON (com histórico)
# ─────────────────────────────────────────────

def load_history(path: str) -> dict:
    """Carrega histórico existente ou cria estrutura inicial."""
    if os.path.exists(path):
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except json.JSONDecodeError:
            pass
    return {"product": "Volante Logitech G29", "source": BASE_URL, "snapshots": []}


def save_snapshot(path: str, items: list[dict]) -> dict:
    """
    Adiciona um snapshot (data + lista de preços) ao histórico JSON.
    Retorna o snapshot recém-criado.
    """
    history = load_history(path)

    snapshot = {
        "scraped_at": datetime.now().isoformat(timespec="seconds"),
        "total_listings": len(items),
        "cheapest_price": min((i["price_value"] for i in items), default=None),
        "average_price": (
            round(sum(i["price_value"] for i in items) / len(items), 2)
            if items else None
        ),
        "listings": items,
    }

    history["snapshots"].append(snapshot)

    with open(path, "w", encoding="utf-8") as f:
        json.dump(history, f, ensure_ascii=False, indent=4)

    return snapshot


# ─────────────────────────────────────────────
# Comparação de Preços entre Snapshots
# ─────────────────────────────────────────────

def compare_prices(path: str) -> None:
    """
    Lê o histórico JSON e exibe uma comparação temporal dos preços:
    mínimo, máximo, médio e variação percentual entre snapshots.
    """
    history = load_history(path)
    snapshots = history.get("snapshots", [])

    if not snapshots:
        print("📭 Nenhum histórico disponível para comparação.")
        return

    print("\n" + "=" * 60)
    print(f"  📊 COMPARAÇÃO DE PREÇOS — {history['product']}")
    print(f"  🛒 Fonte: {history['source']}")
    print("=" * 60)

    all_mins = []
    all_avgs = []

    for i, snap in enumerate(snapshots):
        cheapest = snap.get("cheapest_price")
        average  = snap.get("average_price")
        date_str = snap.get("scraped_at", "?")
        total    = snap.get("total_listings", 0)

        all_mins.append(cheapest)
        all_avgs.append(average)

        label = f"Snapshot #{i + 1}"
        print(f"\n  🕐 {label}  [{date_str}]")
        print(f"     Anúncios válidos : {total}")
        print(f"     Menor preço      : {format_brl(cheapest) if cheapest else 'N/A'}")
        print(f"     Preço médio      : {format_brl(average)  if average  else 'N/A'}")

        # Variação em relação ao snapshot anterior
        if i > 0:
            prev_min = snapshots[i - 1].get("cheapest_price")
            if prev_min and cheapest:
                delta = ((cheapest - prev_min) / prev_min) * 100
                arrow = "📈" if delta > 0 else ("📉" if delta < 0 else "➡️")
                print(f"     Variação (mín.)  : {arrow}  {delta:+.2f}% vs snapshot anterior")

    # Resumo geral
    valid_mins = [v for v in all_mins if v is not None]
    valid_avgs = [v for v in all_avgs if v is not None]

    if valid_mins:
        print("\n" + "-" * 60)
        print("  🏆 RESUMO GERAL")
        print(f"     Menor preço registrado : {format_brl(min(valid_mins))}")
        print(f"     Maior preço registrado : {format_brl(max(valid_mins))}")
        print(f"     Média geral (mínimos)  : {format_brl(sum(valid_mins) / len(valid_mins))}")
        print(f"     Total de snapshots     : {len(snapshots)}")

    # Lista o anúncio mais barato do snapshot mais recente
    latest = snapshots[-1]
    listings = latest.get("listings", [])
    if listings:
        best = min(listings, key=lambda x: x["price_value"])
        print("\n" + "-" * 60)
        print("  🥇 MELHOR OFERTA ATUAL")
        print(f"     Título  : {best['title'][:70]}")
        print(f"     Preço   : {best['price_text']}")
        print(f"     Vendedor: {best['seller']}")
        print(f"     Link    : {best['url'][:80]}")

    print("=" * 60 + "\n")
