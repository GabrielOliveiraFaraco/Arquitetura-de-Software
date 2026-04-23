package service;

import domain.EntityInterface;
import domain.Produto;

import java.util.ArrayList;
import java.util.UUID;

public class ProdutoService implements ServiceInterface{
    ArrayList<Produto> produtos = new ArrayList<>();


    @Override
    public void add(EntityInterface entity) {
        IO.println("Salvando o produto...");
        produtos.add((Produto) entity);
    }

    @Override
    public void remove(EntityInterface entity) {
        IO.println("Removendo o produto...");
        produtos.remove(entity);
    }

    @Override
    public void list() {
        IO.println(produtos);
    }

    @Override
    public void edit(EntityInterface entity) {

    }

    @Override
    public void edit(EntityInterface entity, UUID id) {
        for (int i = 0; i < this.produtos.size(); i++) {
            if (this.produtos.get(i).getId().equals(id)) {
                this.produtos.set(i, ((Produto) entity));
            }
        }
    }


    public Produto buscarPorSku(String sku) {
        for (Produto p : produtos) {
            if (p.getSku().equals(sku)) {
                return p;
            }
        }
        return null;
    }

}
