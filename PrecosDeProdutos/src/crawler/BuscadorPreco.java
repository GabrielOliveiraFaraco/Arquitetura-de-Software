package crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BuscadorPreco {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final List<ExtratorPreco> extratores = new ArrayList<>();
    private final boolean modoSimulado;

    public BuscadorPreco() {
        this(Boolean.parseBoolean(System.getProperty("crawler.simulado", "false")));
    }

    public BuscadorPreco(boolean modoSimulado) {
        this.modoSimulado = modoSimulado;
        extratores.add(new ExtratorPorLoja("amazon"));
        extratores.add(new ExtratorPorLoja("kabum"));
        extratores.add(new ExtratorPorLoja("magalu"));
        extratores.add(new ExtratorPorLoja("magazine"));
        extratores.add(new ExtratorPorLoja("mercadolivre"));
        extratores.add(new ExtratorPorLoja("mercado"));
        extratores.add(new ExtratorPorLoja("casas"));
        extratores.add(new ExtratorGenerico());
    }

    public boolean isModoSimulado() {
        return modoSimulado;
    }

    public Optional<Float> buscarPreco(String loja, String url) {
        if (modoSimulado) {
            return Optional.of(simularPreco(loja, url));
        }

        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(20_000)
                    .followRedirects(true)
                    .get();

            for (ExtratorPreco extrator : extratores) {
                if (!extrator.suporta(loja, url)) {
                    continue;
                }
                Optional<Float> preco = extrator.extrair(document, loja, url);
                if (preco.isPresent()) {
                    return preco;
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            System.out.printf("  [!] Falha ao acessar %s (%s): %s%n", loja, url, e.getMessage());
            return Optional.empty();
        }
    }

    private float simularPreco(String loja, String url) {
        int hash = (loja + url).hashCode();
        float base = 3000f + Math.abs(hash % 1500);
        float variacao = (Math.abs(hash / 17) % 100) / 100f;
        return base + variacao;
    }
}
