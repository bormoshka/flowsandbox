package ru.ulmc.vaadin.service;

import org.springframework.stereotype.Service;
import ru.ulmc.vaadin.entity.MonitoringEvent;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MonitoringService {
    private SecureRandom rand = new SecureRandom();
    private String[] names = new String[]{"Alex", "Boris", "Ivan", "Kate", "Eugen", "Morris"};
    private String[] events = new String[]{"WARN", "ERROR", "INFO", "SPAM"};

    public Collection<MonitoringEvent> getEvents() {
        return IntStream.range(0, 100).mapToObj(operand -> {
            MonitoringEvent event = new MonitoringEvent();
            event.setId(UUID.randomUUID().toString());
            event.setName(events[rand.nextInt(events.length)]);
            event.setUser(names[rand.nextInt(names.length)]);
            event.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
            event.setTimeAndDate(LocalDateTime.now().minusSeconds(rand.nextInt()));
            return event;
        }).collect(Collectors.toList());
    }
}
