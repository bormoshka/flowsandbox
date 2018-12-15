package ru.ulmc.investor.event.listeners;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.function.Consumer;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Registration {
    private final BaseEventListener baseEventListener;
    private final Consumer<BaseEventListener> unregister;

    public void unregister() {
        unregister.accept(baseEventListener);
    }

    static Registration register(BaseEventListener baseEventListener,
                                 Consumer<BaseEventListener> unregister) {
        return new Registration(baseEventListener, unregister);
    }
}
