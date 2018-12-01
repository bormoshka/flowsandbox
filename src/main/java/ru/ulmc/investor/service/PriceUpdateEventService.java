package ru.ulmc.investor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.repository.StockRepository;
import ru.ulmc.investor.event.dto.PriceUpdateEvent;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class PriceUpdateEventService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final StockRepository stockRepository;
    private final MarketService marketService;

    @Autowired
    public PriceUpdateEventService(ApplicationEventPublisher applicationEventPublisher,
                                   StockRepository stockRepository,
                                   MarketService marketService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.stockRepository = stockRepository;
        this.marketService = marketService;
    }


    @Scheduled(initialDelayString = "${ui.positions-page.update-rate}",
            fixedRateString = "${ui.positions-page.update-rate}")
    public void scheduledUpdate() {
        log.trace("Scheduled task {}", Thread.currentThread().getName());
        List<String> symbols = stockRepository.selectAllSymbols();
        if (!symbols.isEmpty()) {
            marketService.getBatchLastPricesAsync(symbols, this::broadcast);
        }
    }

    private void broadcast(Collection<LastPrice> fetchedLastPrices) {
        applicationEventPublisher.publishEvent(new PriceUpdateEvent(fetchedLastPrices));
    }

}
