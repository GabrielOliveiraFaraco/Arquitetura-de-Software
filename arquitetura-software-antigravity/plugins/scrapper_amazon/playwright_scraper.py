import asyncio
import csv
import json
import os
import re
from playwright.async_api import async_playwright

CSV_FILE = "target_products.csv"
OUTPUT_JSON = "amazon_classified.json"

def clean_price(price_str):
    if not price_str:
        return float('inf')
    # Remove non-numeric characters except for comma and dot
    clean_str = re.sub(r'[^\d,.]', '', price_str)
    if not clean_str:
        return float('inf')
    
    # Handle Brazilian format (e.g., 3.999,90 -> 3999.90)
    if ',' in clean_str and '.' in clean_str:
        clean_str = clean_str.replace('.', '').replace(',', '.')
    elif ',' in clean_str:
        clean_str = clean_str.replace(',', '.')
    
    try:
        return float(clean_str)
    except ValueError:
        return float('inf')

async def extract_product_data(page, search_term, required_keywords, banned_keywords):
    print(f"Pesquisando por: {search_term}")
    await page.goto('https://www.amazon.com.br/')
    
    # Preenche a busca e pressiona Enter
    await page.fill('input#twotabsearchtextbox', search_term)
    await page.press('input#twotabsearchtextbox', 'Enter')
    
    # Aguarda o carregamento dos resultados
    try:
        await page.wait_for_selector('[data-component-type="s-search-result"]', timeout=10000)
    except Exception as e:
        print(f"Não foram encontrados resultados para {search_term}: {e}")
        return None

    # Avalia todos os resultados na página
    items = await page.eval_on_selector_all('[data-component-type="s-search-result"]', '''(elements) => {
        return elements.map(el => {
            const titleEl = el.querySelector('h2 span');
            const priceEl = el.querySelector('.a-price > span.a-offscreen');
            const linkEl = el.querySelector('[data-cy="title-recipe"] a') || el.querySelector('h2 a') || el.querySelector('a.a-link-normal.s-no-outline');
            const imgEl = el.querySelector('img.s-image');
            
            let url = '';
            if (linkEl) {
                url = linkEl.href || linkEl.getAttribute('href');
                if (url && !url.startsWith('http')) {
                    url = 'https://www.amazon.com.br' + url;
                }
            }
            
            return {
                title: titleEl ? titleEl.innerText : '',
                price: priceEl ? priceEl.innerText : '',
                url: url,
                image: imgEl ? imgEl.src : ''
            };
        });
    }''')
    
    best_item = None
    lowest_price = float('inf')
    
    # Busca inteligente: filtra e pontua itens
    keywords = [k.lower().strip() for k in required_keywords.split(',')]
    banned = [b.lower().strip() for b in banned_keywords.split(',') if b.strip()]
    
    valid_items = []
    
    for item in items:
        if not item['price'] or not item['title']:
            continue
            
        title_lower = item['title'].lower()
        if any(b in title_lower for b in banned):
            continue
        current_price = clean_price(item['price'])
        
        # Filtro de sanidade extra: Consoles custam muito mais que 500
        min_price = 1500.00 if "quest" in required_keywords or "playstation" in required_keywords else 500.00
        if current_price < min_price:
            continue
            
        match_count = 0
        for k in keywords:
            if k in title_lower:
                match_count += 1
                
        # Exige TODAS as palavras-chave obrigatórias
        if match_count < len(keywords):
            continue
                
        valid_items.append({
            'item': item,
            'price': current_price
        })
        
    if not valid_items:
        return None
        
    # Ordena pelo menor preço entre os itens estritamente válidos
    valid_items.sort(key=lambda x: x['price'])
    
    return valid_items[0]['item']

async def run():
    products = []
    if os.path.exists(CSV_FILE):
        with open(CSV_FILE, mode='r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                products.append(row)
    else:
        print(f"Arquivo CSV não encontrado: {CSV_FILE}")
        return

    results = {}
    
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        # Contexto com locale em pt-BR e User Agent consistente
        context = await browser.new_context(
            locale='pt-BR',
            user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        )
        page = await context.new_page()

        for target in products:
            product_data = await extract_product_data(page, target['search_term'], target['required_keywords'], target.get('banned_keywords', ''))
            
            if product_data:
                category = target['category']
                if category not in results:
                    results[category] = []
                    
                product_data['product_hash'] = target['product_hash']
                results[category].append(product_data)
                print(f"✅ Encontrado: {product_data['title']} - {product_data['price']}")
            else:
                print(f"❌ Nenhum produto válido encontrado para: {target['search_term']}")
                
            # Pausa para não sobrecarregar a Amazon
            await asyncio.sleep(2)

        await browser.close()
        
    # Salva o resultado no JSON final
    with open(OUTPUT_JSON, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=4)
        
    print(f"\\nFinalizado! Dados salvos em '{OUTPUT_JSON}'.")

if __name__ == '__main__':
    asyncio.run(run())
