import domain.Produto;
import service.ProdutoService;

void main() {
    ProdutoService service = new ProdutoService();


    Produto produto = new Produto(
            "436274623",
            "Play5",
            "Sony",
            "VideoGame",
            4000f);
    service.add(produto);
}