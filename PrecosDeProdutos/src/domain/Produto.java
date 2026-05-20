package domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "produto")
public class Produto implements EntityInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sku", length = 64, nullable = false, unique = true)
    private String sku;

    @Column(name = "nome", length = 255, nullable = false)
    private String nome;

    @Column(name = "marca", length = 128)
    private String marca;

    @Column(name = "descricao", length = 1024)
    private String descricao;

    @Column(name = "menor_preco_atual")
    private Float menorPrecoAtual;

    @Column(name = "loja_menor_preco", length = 128)
    private String lojaMenorPreco;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LinkProduto> links = new ArrayList<>();

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Preco> historicoDePrecos = new ArrayList<>();

    public Produto() {
    }

    public Produto(String sku, String nome, String marca, String descricao) {
        this.sku = sku;
        this.nome = nome;
        this.marca = marca;
        this.descricao = descricao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Float getMenorPrecoAtual() {
        return menorPrecoAtual;
    }

    public void setMenorPrecoAtual(Float menorPrecoAtual) {
        this.menorPrecoAtual = menorPrecoAtual;
    }

    public String getLojaMenorPreco() {
        return lojaMenorPreco;
    }

    public void setLojaMenorPreco(String lojaMenorPreco) {
        this.lojaMenorPreco = lojaMenorPreco;
    }

    public List<LinkProduto> getLinks() {
        return links;
    }

    public void setLinks(List<LinkProduto> links) {
        this.links = links;
    }

    public void adicionarLink(LinkProduto link) {
        links.add(link);
        link.setProduto(this);
    }

    public List<Preco> getHistoricoDePrecos() {
        return historicoDePrecos;
    }

    public void setHistoricoDePrecos(List<Preco> historicoDePrecos) {
        this.historicoDePrecos = historicoDePrecos;
    }

    public void registrarMenorPreco(Preco registro) {
        historicoDePrecos.add(registro);
        registro.setProduto(this);
        menorPrecoAtual = registro.getValor();
        lojaMenorPreco = registro.getLoja();
    }

    @Override
    public String toString() {
        return "Produto{" +
                "nome='" + nome + '\'' +
                ", menorPrecoAtual=" + menorPrecoAtual +
                ", lojaMenorPreco='" + lojaMenorPreco + '\'' +
                ", links=" + links.size() +
                '}';
    }
}
