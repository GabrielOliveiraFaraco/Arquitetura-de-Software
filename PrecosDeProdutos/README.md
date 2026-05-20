# Rastreador de Preços de Produtos

Sistema em Java que cadastra produtos com **vários links de lojas**, executa um **crawler** para buscar preços em cada link, compara os valores e salva o **menor preço** no histórico (com nome da loja).

## Requisitos atendidos

- Cadastro de produtos com lista de links (`loja` + `url`)
- Pelo menos **2 lojas diferentes** por produto
- Crawler percorre todos os produtos e todos os links
- Comparação de preços e gravação do menor no histórico
- Histórico com produto, preço, loja e data

## Estrutura do projeto

```
src/
  domain/       # Produto, LinkProduto, Preco
  service/      # CRUD de produtos
  crawler/      # Buscador e extratores de preço por loja
  infra/        # Hibernate + SQLite
  Main.java     # Menu interativo
```

## Como executar

```bash
mvn compile exec:java
```

### Modo simulado (sem acessar sites)

Útil para demonstração em sala de aula quando as lojas bloqueiam requisições automatizadas:

```bash
mvn compile exec:java -Dcrawler.simulado=true
```

## Exemplo de cadastro

Produto **PlayStation 5** com links da Amazon e Kabum:

```json
{
  "nome": "PlayStation 5",
  "links": [
    { "loja": "Amazon", "url": "https://www.amazon.com.br/..." },
    { "loja": "Kabum", "url": "https://www.kabum.com.br/..." }
  ]
}
```

Após executar o crawler (opção 6), o histórico registra algo como:

```
PlayStation 5 | R$ 3699.00 | Kabum | 2026-05-20
```

## Lojas suportadas no crawler

Extratores específicos para: **Amazon**, **Kabum**, **Magalu**, **Mercado Livre**, **Casas Bahia**, além de um extrator genérico (JSON-LD, meta tags e padrões `R$`).

## Banco de dados

SQLite local: `produtos.db` (criado automaticamente na primeira execução).
