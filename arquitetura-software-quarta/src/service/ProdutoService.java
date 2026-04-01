package service;

import domain.EntityInterface;
import domain.Produto;

import java.util.ArrayList;

public class ProdutoService implements ServiceInterface{
    ArrayList<Produto> produtos = new ArrayList<>();


    @Override
    public void add(EntityInterface entity) {
        Produto produto = new Produto();
        produtos.add((Produto) entity);
    }

    @Override
    public void remove(EntityInterface entity) {
        produtos.remove(entity);
    }

    @Override
    public void list() {
        IO.println(produtos);
    }

    @Override
    public void edit(EntityInterface entity) {

    }
}
