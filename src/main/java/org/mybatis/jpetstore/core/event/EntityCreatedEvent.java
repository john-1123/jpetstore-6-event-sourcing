package org.mybatis.jpetstore.core.event;

public class EntityCreatedEvent extends DomainEvent {
    public EntityCreatedEvent(String id, String entityType, long timestamp) {
        super(id, entityType, timestamp);
    }
}
