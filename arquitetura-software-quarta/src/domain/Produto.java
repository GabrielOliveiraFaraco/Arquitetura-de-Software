package domain;

import java.util.ArrayList;
import java.util.UUID;

public class Produto implements EntityInterface{

    private UUID id;
    private String sku;
    private String nome;
    private String marca;
    private String descricao;
    private Float preco;
    private ArrayList<Preco> historicoDePrecos;

    public Produto() {
    }

    public Produto(UUID id, String sku, String nome, String marca, String descricao, Float preco) {
        this.sku = sku;
        this.nome = nome;
        this.marca = marca;
        this.descricao = descricao;
        this.preco = preco;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public Float getPreco() {
        return preco;
    }

    public void setPreco(Float preco) {
        this.preco = preco;
    }

    public ArrayList<Preco> getHistoricoDePrecos() {
        return historicoDePrecos;
    }

    public void setHistoricoDePrecos(ArrayList<Preco> historicoDePrecos) {
        this.historicoDePrecos = historicoDePrecos;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", nome='" + nome + '\'' +
                ", marca='" + marca + '\'' +
                ", descricao='" + descricao + '\'' +
                ", preco=" + preco +
                ", historicoDePrecos=" + historicoDePrecos +
                '}';
    }
}
