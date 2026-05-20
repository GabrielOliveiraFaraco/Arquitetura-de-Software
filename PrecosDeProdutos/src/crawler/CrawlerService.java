package crawler;

import domain.LinkProduto;
import domain.Preco;
import domain.Produto;
import infra.HibernateUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CrawlerService {

    private static final int MINIMO_LOJAS = 2;

    private final BuscadorPreco buscadorPreco = new BuscadorPreco();

    public void executar() {
        List<Produto> produtos = carregarProdutos();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado. Cadastre produtos com links antes de executar o crawler.");
            return;
        }

        System.out.println("\n=== Iniciando crawler de preços ===\n");
        if (buscadorPreco.isModoSimulado()) {
            System.out.println("Modo simulado ativo (crawler.simulado=true).\n");
        }

        int processados = 0;
        for (Produto produto : produtos) {
            if (processarProduto(produto)) {
                processados++;
            }
        }

        System.out.printf("%nCrawler finalizado. %d produto(s) atualizado(s).%n", processados);
    }

    private boolean processarProduto(Produto produto) {
        List<LinkProduto> links = produto.getLinks();
        if (links == null || links.size() < MINIMO_LOJAS) {
            System.out.printf("[!] %s ignorado: cadastre ao menos %d links de lojas diferentes.%n",
                    produto.getNome(), MINIMO_LOJAS);
            return false;
        }

        System.out.printf("Produto: %s%n", produto.getNome());
        List<PrecoEncontrado> precos = new ArrayList<>();

        for (LinkProduto link : links) {
            buscadorPreco.buscarPreco(link.getLoja(), link.getUrl()).ifPresentOrElse(
                    valor -> {
                        precos.add(new PrecoEncontrado(link.getLoja(), link.getUrl(), valor));
                        System.out.printf("  - %s: R$ %.2f%n", link.getLoja(), valor);
                    },
                    () -> System.out.printf("  - %s: preço não encontrado%n", link.getLoja()));
        }

        if (precos.isEmpty()) {
            System.out.println("  Nenhum preço válido encontrado.\n");
            return false;
        }

        PrecoEncontrado menor = precos.stream()
                .min(Comparator.comparing(PrecoEncontrado::valor))
                .orElseThrow();

        salvarMenorPreco(produto.getId(), menor);
        System.out.printf("  => Menor preço: R$ %.2f na %s%n%n", menor.valor(), menor.loja());
        return true;
    }

    private void salvarMenorPreco(java.util.UUID produtoId, PrecoEncontrado menor) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Produto managed = session.get(Produto.class, produtoId);
            if (managed != null) {
                Preco registro = new Preco(new Date(), menor.valor(), menor.loja(), managed);
                managed.registrarMenorPreco(registro);
                session.persist(registro);
            }
            tx.commit();
        }
    }

    private List<Produto> carregarProdutos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Produto> produtos = session.createQuery(
                            "select distinct p from Produto p "
                                    + "left join fetch p.links "
                                    + "order by p.nome",
                            Produto.class)
                    .getResultList();
            produtos.forEach(produto -> produto.getHistoricoDePrecos().size());
            return produtos;
        }
    }
}
