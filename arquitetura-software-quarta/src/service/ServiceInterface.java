package service;

import domain.EntityInterface;

public interface ServiceInterface {
    void add(EntityInterface entity);
    void remove(EntityInterface entity);
    void list();
    void edit(EntityInterface entity);

}
