package crawler;

import java.util.Optional;
import org.jsoup.nodes.Document;

public interface ExtratorPreco {
    boolean suporta(String loja, String url);

    Optional<Float> extrair(Document document, String loja, String url);
}
