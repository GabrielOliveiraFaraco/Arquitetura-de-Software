package domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "preco")
public class Preco implements EntityInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Temporal(TemporalType.DATE)
    @Column(name = "data_registro", nullable = false)
    private Date data;

    @Column(name = "preco", nullable = false)
    private Float valor;

    @Column(name = "loja", length = 128, nullable = false)
    private String loja;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    public Preco() {
    }

    public Preco(Date data, Float valor, String loja, Produto produto) {
        this.data = data;
        this.valor = valor;
        this.loja = loja;
        this.produto = produto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }

    public String getLoja() {
        return loja;
    }

    public void setLoja(String loja) {
        this.loja = loja;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }
}
