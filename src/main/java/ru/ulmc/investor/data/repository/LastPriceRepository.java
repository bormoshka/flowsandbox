package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import ru.ulmc.investor.data.entity.InnerQuote;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.entity.Symbol;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LastPriceRepository extends CrudRepository<LastPrice, UUID> {

    LastPrice findFirstBySymbolAndDateTime(String symbol, LocalDateTime time);

    LastPrice findFirstBySymbolOrderByDateTimeDesc(String symbol);
}
