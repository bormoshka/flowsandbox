package ru.ulmc.investor.event.listeners;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.tomcat.util.collections.ManagedConcurrentWeakHashMap;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;
import ru.ulmc.investor.event.dto.PriceUpdateEvent;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Collections.emptySet;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

@Slf4j
@Component
public class StaticUpdateBroadcaster implements ApplicationListener<PayloadApplicationEvent<PriceUpdateEvent>> {

    private final Map<Class, Set<BaseEventListener<PriceUpdateEvent>>> listeners = new ManagedConcurrentWeakHashMap<>();

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<PriceUpdateEvent> event) {
        val baseEventListeners = listeners.get(PriceUpdateEvent.class);
        if (isNotEmpty(baseEventListeners)) {
            log.debug("Sending inner event to listeners {}", baseEventListeners);
            PriceUpdateEvent payload = event.getPayload();
            baseEventListeners.forEach(listener -> listener.receive(payload));
        }
    }

    public Registration subscribe(Consumer<PriceUpdateEvent> eventConsumer) {
        val listener = BaseEventListener.from(eventConsumer);
        listeners.computeIfAbsent(PriceUpdateEvent.class, s -> new HashSet<>()).add(listener);
        return Registration.register(listener, eventListener ->
                listeners.getOrDefault(PriceUpdateEvent.class, emptySet()).remove(eventListener));
    }

    public boolean hasListenersFor(Class event) {
       return isNotEmpty(listeners.get(event));
    }
}
