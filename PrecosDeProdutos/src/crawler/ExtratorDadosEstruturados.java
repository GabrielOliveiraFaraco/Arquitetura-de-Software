package crawler;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Extrai preço de metadados e JSON-LD da página do produto, evitando preços de itens relacionados.
 */
public final class ExtratorDadosEstruturados {

    private static final Pattern TIPO_PRODUTO = Pattern.compile(
            "\"@type\"\\s*:\\s*\"Product\"",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PRECO_JSON_LD = Pattern.compile(
            "\"(?:price|lowPrice)\"\\s*:\\s*\"?([\\d]+(?:[.,][\\d]+)?)\"?",
            Pattern.CASE_INSENSITIVE);

    private ExtratorDadosEstruturados() {
    }

    public static Optional<Float> extrair(Document document) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            Optional<Float> preco = extrairDeJsonLd(script.data());
            if (preco.isPresent()) {
                return preco;
            }
        }

        for (Element meta : document.select("meta[property=product:price:amount], meta[property=og:price:amount]")) {
            Optional<Float> preco = PrecoParser.parseNumero(meta.attr("content"));
            if (preco.isPresent()) {
                return preco;
            }
        }

        Element metaPreco = document.selectFirst("meta[itemprop=price]");
        if (metaPreco != null) {
            Optional<Float> preco = PrecoParser.parseNumero(metaPreco.attr("content"));
            if (preco.isPresent()) {
                return preco;
            }
        }

        Element itemprop = document.selectFirst("[itemprop=price][content]");
        if (itemprop != null) {
            Optional<Float> preco = PrecoParser.parseNumero(itemprop.attr("content"));
            if (preco.isPresent()) {
                return preco;
            }
        }

        return Optional.empty();
    }

    static Optional<Float> extrairDeJsonLd(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        if (!TIPO_PRODUTO.matcher(json).find()) {
            return Optional.empty();
        }

        Matcher matcher = PRECO_JSON_LD.matcher(json);
        while (matcher.find()) {
            Optional<Float> preco = PrecoParser.parseNumero(matcher.group(1));
            if (preco.isPresent()) {
                return preco;
            }
        }
        return Optional.empty();
    }
}
