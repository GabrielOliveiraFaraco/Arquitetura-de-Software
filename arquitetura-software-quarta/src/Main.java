import domain.Produto;
import service.ProdutoService;

void main() {
    ProdutoService service = new ProdutoService();

    UUID id = new UUID(1, 1);
    Produto produto = new Produto(
            id,
            "436274623",
            "Play5",
            "Sony",
            "VideoGame",
            4000f);

    service.list();

    boolean isGoing = true;

    while (isGoing){
        int escolha = Integer.parseInt(IO.readln("Escolha uma ação: 1 - Adicionar, 2 - Remover, 3 - Listar, 4 - Editar, 0 - Sair: "));
        switch (escolha) {
            case 1:
                UUID id2 = new UUID(2 ,2);
                String sku = IO.readln("SKU: ");
                String nome = IO.readln("Nome: ");
                String marca = IO.readln("Marca: ");
                String descricao = IO.readln("Descrição: ");
                Float preco = Float.parseFloat(IO.readln("Preço: "));
                Produto produto1 = new Produto(id2, sku, nome, marca, descricao, preco);
                produto1.setId(id2);
                service.add(produto1);
                break;
            case 2:
                String skuRemove = IO.readln("Digite o SKU que deseja deletar: ");
                Produto produtoRemove = service.buscarPorSku(skuRemove);
                if (produtoRemove != null) {
                    service.remove(produtoRemove);
                    IO.println("Produto removido com sucesso!");
                } else {
                    IO.println("Produto não encontrado com o SKU informado.");
                }
                break;
            case 3:
                service.list();
                break;
            case 4:
                String skuParaEditar = IO.readln("Digite o SKU do produto que deseja editar: ");
                Produto produtoExistente = service.buscarPorSku(skuParaEditar);

                if (produtoExistente != null) {
                    String novoSku = IO.readln("Novo SKU (deixe em branco para manter): ");
                    String novoNome = IO.readln("Novo nome: ");
                    String novaMarca = IO.readln("Nova marca: ");
                    String novaDescricao = IO.readln("Nova descrição: ");
                    Float novoPreco = Float.parseFloat(IO.readln("Novo preço: "));

                    if (novoSku.trim().isEmpty()) novoSku = produtoExistente.getSku();
                    if (novoNome.trim().isEmpty()) novoNome = produtoExistente.getNome();
                    if (novaMarca.trim().isEmpty()) novaMarca = produtoExistente.getMarca();
                    if (novaDescricao.trim().isEmpty()) novaDescricao = produtoExistente.getDescricao();
                    if (novoPreco == null || novoPreco == 0) novoPreco = produtoExistente.getPreco();

                    Produto produtoEditado = new Produto(
                            produtoExistente.getId(),
                            novoSku,
                            novoNome,
                            novaMarca,
                            novaDescricao,
                            novoPreco
                    );

                    service.edit(produtoEditado, produtoExistente.getId());
                    IO.println("Produto editado com sucesso!");
                } else {
                    IO.println("Produto não encontrado!");
                }
                break;
            case 0:
                IO.println("Saindo...");
                isGoing = false;
                break;

        }

    }

}