package crawler;

import java.util.Optional;
import org.jsoup.nodes.Document;

public class ExtratorGenerico implements ExtratorPreco {

    @Override
    public boolean suporta(String loja, String url) {
        return true;
    }

    @Override
    public Optional<Float> extrair(Document document, String loja, String url) {
        Optional<Float> estruturado = ExtratorDadosEstruturados.extrair(document);
        if (estruturado.isPresent()) {
            return estruturado;
        }

        String[] escopos = {"main", "[role=main]", "#product", ".product", ".product-detail", ".pdp"};
        String[] seletores = {
                "[data-testid=price-value]",
                ".finalPrice",
                ".sales-price"
        };

        for (String escopo : escopos) {
            var area = document.selectFirst(escopo);
            if (area == null) {
                continue;
            }
            for (String seletor : seletores) {
                Optional<Float> preco = primeiroPrecoEm(area, seletor);
                if (preco.isPresent()) {
                    return preco;
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<Float> primeiroPrecoEm(org.jsoup.nodes.Element raiz, String seletor) {
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
        return Optional.empty();
    }
}
