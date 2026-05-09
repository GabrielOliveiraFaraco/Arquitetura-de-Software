package service;

import domain.EntityInterface;
import domain.Preco;
import infra.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.UUID;

public class PrecoService implements ServiceInterface {

    @Override
    public void add(EntityInterface entity) {
        // Not implemented
    }

    @Override
    public void remove(EntityInterface entity) {
        // Not implemented
    }

    @Override
    public void list() {
        List<Preco> precos = listar();
        for (int i = 0; i < precos.size(); i++) {
            Preco p = precos.get(i);
            System.out.printf("\nIndice: %s\n", i);
            System.out.printf("Id: %s\n", p.getId());
            System.out.printf("Preço: %s\n", p.getPreco());
            System.out.printf("Data: %s\n", p.getDataAtual());
            if (p.getProduto() != null) {
                System.out.printf("Produto: %s\n", p.getProduto().getNome());
            }
            System.out.println("---------------------------------\n");
        }
    }

    @Override
    public void edit(EntityInterface entity, UUID id) {
        System.out.println("Editando o preço");
        Preco atualizado = (Preco) entity;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Preco managed = session.get(Preco.class, id);
            if (managed != null) {
                managed.setPreco(atualizado.getPreco());
            }
            tx.commit();
        }
    }

    @Override
    public EntityInterface findByIndex(int index) {
        List<Preco> precos = listar();
        if (index >= 0 && index < precos.size()) {
            return precos.get(index);
        }
        return null;
    }

    private List<Preco> listar() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Preco order by dataAtual desc", Preco.class).getResultList();
        }
    }
}
