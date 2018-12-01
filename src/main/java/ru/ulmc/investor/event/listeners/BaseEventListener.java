package ru.ulmc.investor.event.listeners;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.ulmc.investor.event.dto.BaseEvent;

import java.util.UUID;
import java.util.function.Consumer;

@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class BaseEventListener<T extends BaseEvent> {
    @Getter
    private final String id = UUID.randomUUID().toString();
    private final Consumer<T> eventConsumer;

    void receive(T event) {
        eventConsumer.accept(event);
    }

    static <T extends BaseEvent> BaseEventListener<T> from(Consumer<T> eventConsumer) {
        return new BaseEventListener<>(eventConsumer);
    }
}
