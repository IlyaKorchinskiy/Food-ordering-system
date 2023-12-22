package com.food.ordering.system.domain.events;

public final class EmptyEvent implements DomainEvent<Void> {

    public static final EmptyEvent INSTANCE = new EmptyEvent();

    private EmptyEvent() {}

    @Override
    public void fire() {}
}
