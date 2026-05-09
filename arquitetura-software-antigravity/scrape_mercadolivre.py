"""
scrape_mercadolivre.py -- Entry point para o scraper do Mercado Livre.

Uso:
    python scrape_mercadolivre.py            # Raspa + compara precos
    python scrape_mercadolivre.py --compare  # Apenas exibe historico (sem raspar)
"""

import asyncio
import sys
import os

# Garante UTF-8 no terminal Windows (PowerShell / CMD)
if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

# Garante que o projeto consegue importar pacotes da raiz
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from plugins.scrapper_mercadolivre.ml_scraper import (
    scrape_g29_prices,
    save_snapshot,
    compare_prices,
    format_brl,
    OUTPUT_JSON,
)


def print_banner():
    print("\n" + "=" * 60)
    print("  [ML]  MERCADO LIVRE -- RASTREADOR DE PRECOS")
    print("        Volante Logitech G29")
    print("=" * 60 + "\n")


async def main():
    print_banner()

    only_compare = "--compare" in sys.argv

    if not only_compare:
        print("🕷️  Iniciando scraping no Mercado Livre...")
        items = await scrape_g29_prices()

        if not items:
            print("❌  Nenhum anúncio válido encontrado.")
            print("    Verifique sua conexão ou tente novamente mais tarde.\n")
        else:
            print(f"\n✅  {len(items)} anúncio(s) válido(s) encontrado(s):\n")
            for i, item in enumerate(items, 1):
                print(f"  {i:02d}. {item['title'][:65]}")
                print(f"       💰 {item['price_text']}  |  🏪 {item['seller']}")
                print(f"       🔗 {item['url'][:70]}\n")

            snapshot = save_snapshot(OUTPUT_JSON, items)
            print(f"💾  Snapshot salvo em '{OUTPUT_JSON}'")
            print(f"    Menor preço capturado: {format_brl(snapshot['cheapest_price'])}")
            print(f"    Preço médio dos anúncios: {format_brl(snapshot['average_price'])}")

    # Sempre exibe a comparação histórica
    compare_prices(OUTPUT_JSON)


if __name__ == "__main__":
    asyncio.run(main())
