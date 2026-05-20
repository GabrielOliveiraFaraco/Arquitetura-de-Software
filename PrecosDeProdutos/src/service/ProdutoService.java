package service;

import domain.EntityInterface;
import domain.LinkProduto;
import domain.Preco;
import domain.Produto;
import infra.HibernateUtil;
import java.util.List;
import java.util.UUID;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ProdutoService implements ServiceInterface {

    public static final int MINIMO_LINKS = 2;

    @Override
    public void add(EntityInterface entity) {
        Produto produto = (Produto) entity;
        validarLinks(produto);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(produto);
            tx.commit();
        }
        System.out.println("Produto cadastrado com sucesso.");
    }

    @Override
    public void remove(EntityInterface entity) {
        Produto produto = (Produto) entity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Produto managed = session.get(Produto.class, produto.getId());
            if (managed != null) {
                session.remove(managed);
            }
            tx.commit();
        }
        System.out.println("Produto removido.");
    }

    @Override
    public void list() {
        List<Produto> produtos = listar();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }

        for (int i = 0; i < produtos.size(); i++) {
            imprimirProduto(i, produtos.get(i));
        }
    }

    @Override
    public EntityInterface findByIndex(int index) {
        return listar().get(index);
    }

    @Override
    public void edit(EntityInterface entity, UUID id) {
        Produto atualizado = (Produto) entity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Produto managed = session.get(Produto.class, id);
            if (managed != null) {
                managed.setSku(atualizado.getSku());
                managed.setNome(atualizado.getNome());
                managed.setMarca(atualizado.getMarca());
                managed.setDescricao(atualizado.getDescricao());
            }
            tx.commit();
        }
        System.out.println("Produto atualizado.");
    }

    public void adicionarLink(UUID produtoId, String loja, String url) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Produto managed = session.get(Produto.class, produtoId);
            if (managed != null) {
                LinkProduto link = new LinkProduto(loja, url, managed);
                managed.adicionarLink(link);
                session.persist(link);
            }
            tx.commit();
        }
    }

    public Produto buscarComLinks(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select p from Produto p left join fetch p.links where p.id = :id",
                            Produto.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    private List<Produto> listar() {
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

    private void imprimirProduto(int indice, Produto produto) {
        System.out.printf("%n[%d] %s (SKU: %s)%n", indice, produto.getNome(), produto.getSku());
        System.out.printf("Marca: %s%n", produto.getMarca());
        System.out.printf("Descrição: %s%n", produto.getDescricao());

        if (produto.getMenorPrecoAtual() != null) {
            System.out.printf("Menor preço atual: R$ %.2f%n", produto.getMenorPrecoAtual());
            System.out.printf("Loja: %s%n", produto.getLojaMenorPreco());
        } else {
            System.out.println("Menor preço atual: ainda não coletado pelo crawler.");
        }

        System.out.println("Links cadastrados:");
        if (produto.getLinks() == null || produto.getLinks().isEmpty()) {
            System.out.println("  (nenhum link)");
        } else {
            for (LinkProduto link : produto.getLinks()) {
                System.out.printf("  - %s: %s%n", link.getLoja(), link.getUrl());
            }
        }

        if (produto.getHistoricoDePrecos() != null && !produto.getHistoricoDePrecos().isEmpty()) {
            System.out.println("Histórico de preços (menor preço por execução):");
            for (Preco registro : produto.getHistoricoDePrecos()) {
                System.out.printf(
                        "  - %s | R$ %.2f | %s%n",
                        registro.getData(),
                        registro.getValor(),
                        registro.getLoja());
            }
        }
        System.out.println("---------------------------------");
    }

    private void validarLinks(Produto produto) {
        if (produto.getLinks() == null || produto.getLinks().size() < MINIMO_LINKS) {
            throw new IllegalArgumentException(
                    "Cadastre ao menos " + MINIMO_LINKS + " links de lojas diferentes para o produto.");
        }

        long lojasDistintas = produto.getLinks().stream()
                .map(link -> link.getLoja().trim().toLowerCase())
                .distinct()
                .count();

        if (lojasDistintas < MINIMO_LINKS) {
            throw new IllegalArgumentException(
                    "Os links devem apontar para pelo menos " + MINIMO_LINKS + " lojas diferentes.");
        }
    }
}
