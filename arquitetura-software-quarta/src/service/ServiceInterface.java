package service;

import domain.EntityInterface;

import java.util.UUID;

public interface ServiceInterface {
    void add(EntityInterface entity);
    void remove(EntityInterface entity);
    void list();
    void edit(EntityInterface entity);

    void edit(EntityInterface entity, UUID id);
}
