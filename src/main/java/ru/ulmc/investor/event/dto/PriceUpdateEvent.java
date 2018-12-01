package ru.ulmc.investor.event.dto;

import lombok.Value;
import ru.ulmc.investor.data.entity.LastPrice;

import java.util.Collection;

@Value
public class PriceUpdateEvent implements BaseEvent<Collection<LastPrice>> {
    private final Collection<LastPrice> lastPrices;

    @Override
    public Collection<LastPrice> getData() {
        return lastPrices;
    }
}
