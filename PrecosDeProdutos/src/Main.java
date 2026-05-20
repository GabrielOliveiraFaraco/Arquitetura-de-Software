import crawler.CrawlerService;
import domain.LinkProduto;
import domain.Produto;
import infra.HibernateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import service.ProdutoService;
import service.ServiceInterface;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        ProdutoService produtoService = new ProdutoService();
        CrawlerService crawlerService = new CrawlerService();

        try {
            boolean menuAtivo = true;
            while (menuAtivo) {
                int opcao = menu();
                switch (opcao) {
                    case 1 -> adicionarProduto(produtoService);
                    case 2 -> produtoService.list();
                    case 3 -> editarProduto(produtoService);
                    case 4 -> deletarProduto(produtoService);
                    case 5 -> adicionarLink(produtoService);
                    case 6 -> crawlerService.executar();
                    case 0 -> menuAtivo = false;
                    default -> System.out.println("Opcao invalida.");
                }
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void adicionarProduto(ProdutoService service) {
        String sku = lerTexto("SKU do produto: ");
        String nome = lerTexto("Nome do produto: ");
        String marca = lerTexto("Marca: ");
        String descricao = lerTexto("Descricao: ");

        Produto produto = new Produto(sku, nome, marca, descricao);
        List<LinkProduto> links = coletarLinks();
        for (LinkProduto link : links) {
            produto.adicionarLink(link);
        }

        try {
            service.add(produto);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static List<LinkProduto> coletarLinks() {
        List<LinkProduto> links = new ArrayList<>();
        System.out.println("Cadastre os links do produto (minimo 2 lojas diferentes).");
        System.out.println("Deixe a loja em branco para encerrar.");

        while (true) {
            String loja = lerTexto("Loja (ex: Amazon, Kabum): ").trim();
            if (loja.isEmpty()) {
                break;
            }
            String url = lerTexto("URL do produto nessa loja: ").trim();
            if (url.isEmpty()) {
                System.out.println("URL obrigatoria. Tente novamente.");
                continue;
            }
            links.add(new LinkProduto(loja, url, null));
        }
        return links;
    }

    private static void editarProduto(ServiceInterface service) {
        System.out.println("Produtos cadastrados:");
        service.list();
        int indice = lerInteiro("Indice do produto a editar: ");
        Produto produto = (Produto) service.findByIndex(indice);

        produto.setSku(lerTexto("Novo SKU: "));
        produto.setNome(lerTexto("Novo nome: "));
        produto.setMarca(lerTexto("Nova marca: "));
        produto.setDescricao(lerTexto("Nova descricao: "));
        service.edit(produto, produto.getId());
    }

    private static void deletarProduto(ServiceInterface service) {
        System.out.println("Produtos cadastrados:");
        service.list();
        int indice = lerInteiro("Indice do produto a deletar: ");
        Produto produto = (Produto) service.findByIndex(indice);
        service.remove(produto);
    }

    private static void adicionarLink(ProdutoService service) {
        System.out.println("Produtos cadastrados:");
        service.list();
        int indice = lerInteiro("Indice do produto: ");
        Produto produto = (Produto) service.findByIndex(indice);

        String loja = lerTexto("Loja: ").trim();
        String url = lerTexto("URL: ").trim();
        service.adicionarLink(produto.getId(), loja, url);
        System.out.println("Link adicionado.");
    }

    private static int menu() {
        System.out.println();
        System.out.println("=== Rastreador de Precos ===");
        System.out.println("1 - Cadastrar produto (com links)");
        System.out.println("2 - Listar produtos e historico");
        System.out.println("3 - Editar produto");
        System.out.println("4 - Deletar produto");
        System.out.println("5 - Adicionar link a um produto");
        System.out.println("6 - Executar crawler");
        System.out.println("0 - Sair");
        return lerInteiro("Opcao: ");
    }

    private static String lerTexto(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }

    private static int lerInteiro(String prompt) {
        while (true) {
            String valor = lerTexto(prompt).trim();
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero valido.");
            }
        }
    }
}
