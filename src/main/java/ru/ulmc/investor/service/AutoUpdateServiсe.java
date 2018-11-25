package ru.ulmc.investor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AutoUpdateServiсe {
    private final ApplicationEventMulticaster eventMulticaster;

    @Autowired
    public AutoUpdateServiсe(ApplicationEventMulticaster eventMulticaster) {
        this.eventMulticaster = eventMulticaster;
    }


    @Scheduled(initialDelayString = "${ui.positions-page.update-rate}",
            fixedRateString = "${ui.positions-page.update-rate}")
    public void scheduledUpdate() {
        log.trace("Scheduled task {}", Thread.currentThread().getName());

    }

}
