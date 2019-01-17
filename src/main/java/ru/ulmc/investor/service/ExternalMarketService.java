package ru.ulmc.investor.service;

import pl.zankowski.iextrading4j.api.stocks.Company;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import ru.ulmc.investor.data.entity.HistoryPrice;
import ru.ulmc.investor.data.entity.InnerQuote;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.service.dto.KeyStatsDto;

public interface ExternalMarketService {

    void getLastPriceAsync(Collection<String> symbols, Consumer<Collection<LastPrice>> quoteConsumer);

    Collection<LastPrice> getLastPrice(Collection<String> symbols);

    Future<InnerQuote> getQuoteAsync(String symbol);

    void getQuoteAsync(String symbol, Consumer<InnerQuote> quoteConsumer);

    Optional<Company> getCompany(String symbol);

    void subscribeForLastTrade(Collection<String> symbols, Consumer<LastPrice> lastTradeConsumer);

    void getLastMonthHistoryData(String symbol, Consumer<Collection<HistoryPrice>> priceConsumer);

    List<KeyStatsDto> getKeyStats(Collection<String> symbols);

    void getKeyStatsAsync(Collection<String> symbols, Consumer<Collection<KeyStatsDto>> resultConsumer);
}
