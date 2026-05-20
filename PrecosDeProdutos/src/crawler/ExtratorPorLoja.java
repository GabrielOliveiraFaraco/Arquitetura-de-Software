package crawler;

import java.util.Locale;
import java.util.Optional;
import org.jsoup.nodes.Document;

public class ExtratorPorLoja implements ExtratorPreco {

    private final String chave;

    public ExtratorPorLoja(String chave) {
        this.chave = chave.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean suporta(String loja, String url) {
        String lojaNormalizada = loja == null ? "" : loja.toLowerCase(Locale.ROOT);
        String urlNormalizada = url == null ? "" : url.toLowerCase(Locale.ROOT);
        return lojaNormalizada.contains(chave) || urlNormalizada.contains(chave);
    }

    @Override
    public Optional<Float> extrair(Document document, String loja, String url) {
        Optional<Float> estruturado = ExtratorDadosEstruturados.extrair(document);
        if (estruturado.isPresent()) {
            return estruturado;
        }

        return switch (chave) {
            case "amazon" -> extrairAmazon(document);
            case "kabum" -> extrairKabum(document);
            case "magalu", "magazine" -> extrairMagalu(document);
            case "mercado" -> extrairMercadoLivre(document);
            case "casas" -> extrairCasasBahia(document);
            default -> Optional.empty();
        };
    }

    private Optional<Float> extrairAmazon(Document document) {
        return primeiroPrecoNoEscopo(document,
                "#dp-container, #centerCol, #ppd",
                "#corePrice_feature_div .a-offscreen",
                "#corePriceDisplay_desktop_feature_div .a-offscreen",
                "#apex_desktop .a-offscreen",
                "#priceblock_ourprice",
                "#priceblock_dealprice");
    }

    private Optional<Float> extrairKabum(Document document) {
        return primeiroPrecoNoEscopo(document,
                ".mainBox, .container-main, #container-product-detail",
                "h4[class*=finalPrice]",
                ".finalPrice",
                "[class*=priceCard] h4");
    }

    private Optional<Float> extrairMagalu(Document document) {
        return primeiroPrecoNoEscopo(document,
                "[data-testid=product-price], .product-detail, .pdp",
                "[data-testid=price-value]",
                "p[data-testid=price-value]");
    }

    private Optional<Float> extrairMercadoLivre(Document document) {
        return primeiroPrecoNoEscopo(document,
                ".ui-pdp-container, #price, .ui-pdp-price",
                ".ui-pdp-price__second-line .andes-money-amount",
                "#price .andes-money-amount",
                ".ui-pdp-price .andes-money-amount");
    }

    private Optional<Float> extrairCasasBahia(Document document) {
        return primeiroPrecoNoEscopo(document,
                "[data-testid=product-price], .product-detail",
                "[data-testid=price-value]",
                ".sales-price");
    }

    private Optional<Float> primeiroPrecoNoEscopo(Document document, String escopo, String... seletores) {
        var area = document.selectFirst(escopo);
        if (area != null) {
            Optional<Float> preco = primeiroPreco(area, seletores);
            if (preco.isPresent()) {
                return preco;
            }
        }
        return primeiroPreco(document, seletores);
    }

    private Optional<Float> primeiroPreco(org.jsoup.nodes.Element raiz, String... seletores) {
        for (String seletor : seletores) {
            for (var elemento : raiz.select(seletor)) {
                String conteudo = elemento.hasAttr("content")
                        ? elemento.attr("content")
                        : elemento.text();
                Optional<Float> preco = PrecoParser.parseNumero(conteudo)
                        .or(() -> PrecoParser.parseTexto(conteudo));
                if (preco.isPresent()) {
                    return preco;
                }
            }
        }
        return Optional.empty();
    }
}
