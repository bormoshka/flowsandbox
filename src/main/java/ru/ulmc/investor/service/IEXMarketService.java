package ru.ulmc.investor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import pl.zankowski.iextrading4j.api.exception.IEXTradingException;
import pl.zankowski.iextrading4j.api.marketdata.LastTrade;
import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.ChartRange;
import pl.zankowski.iextrading4j.api.stocks.Company;
import pl.zankowski.iextrading4j.api.stocks.KeyStats;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.manager.RestRequest;
import pl.zankowski.iextrading4j.client.rest.request.marketdata.LastTradeRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.ChartRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.CompanyRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.KeyStatsRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;
import pl.zankowski.iextrading4j.client.socket.request.marketdata.LastAsyncRequestBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.ulmc.investor.data.entity.HistoryPrice;
import ru.ulmc.investor.data.entity.InnerQuote;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.service.convert.IEXMarketConverter;
import ru.ulmc.investor.service.dto.KeyStatsDto;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@Profile("external-sources")
public class IEXMarketService implements ExternalMarketService {
    private final IEXTradingClient tradingClient;
    private final IEXMarketConverter converter;

    @Autowired
    public IEXMarketService(IEXMarketConverter converter) {
        this.converter = converter;
        tradingClient = IEXTradingClient.create();
    }

    @PreDestroy
    public void preDestroy() {
        // tradingClient close?
    }

    @Async
    @Override
    public void getLastPriceAsync(Collection<String> symbols, Consumer<Collection<LastPrice>> quoteConsumer) {
        List<LastTrade> trades = executeLastRequest(symbols);
        Set<LastPrice> prices = trades.stream().map(converter::convert).collect(toSet());
        quoteConsumer.accept(prices);
    }

    @Override
    public Collection<LastPrice> getLastPrice(Collection<String> symbols) {
        List<LastTrade> trades = executeLastRequest(symbols);
        return trades.stream().map(converter::convert).collect(toSet());
    }

    @Async
    @Override
    public Future<InnerQuote> getQuoteAsync(String symbol) {
        Quote quote = executeQuoteRequest(symbol);
        return new AsyncResult<>(converter.convert(quote));
    }

    @Async
    @Override
    public void getQuoteAsync(String symbol, Consumer<InnerQuote> quoteConsumer) {
        Quote quote = executeQuoteRequest(symbol);
        quoteConsumer.accept(converter.convert(quote));
    }

    @Override
    public Optional<Company> getCompany(String symbol) {
        RestRequest<Company> restRequest = new CompanyRequestBuilder().withSymbol(symbol).build();
        try {
            return Optional.of(tradingClient.executeRequest(restRequest));
        } catch (IEXTradingException ex) {
            log.trace("Symbol {} not found", symbol);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Fetching company with symbol: {}", symbol, ex);
            return Optional.empty();
        }
    }

    /**
     * Подписывается на изменения по инструментам.
     *
     * @param symbols           Набор инструментов для подписания
     * @param lastTradeConsumer потребитель обновлений
     */
    @Override
    public void subscribeForLastTrade(Collection<String> symbols,
                                      Consumer<LastPrice> lastTradeConsumer) {
        LastAsyncRequestBuilder requestBuilder = new LastAsyncRequestBuilder();
        symbols.forEach(requestBuilder::withSymbol);

        tradingClient.subscribe(requestBuilder.build(),
                trade -> lastTradeConsumer.accept(converter.convert(trade)));
    }

    @Async
    @Override
    public void getLastMonthHistoryData(String symbol, Consumer<Collection<HistoryPrice>> priceConsumer) {
        List<Chart> charts = executeChartRequest(symbol, ChartRange.ONE_MONTH);
        priceConsumer.accept(converter.convert(symbol, charts));
    }

    @Override
    public List<KeyStatsDto> getKeyStats(Collection<String> symbols) {
        return getKeyStatsInner(symbols);
    }

    @Async
    @Override
    public void getKeyStatsAsync(Collection<String> symbols,
                                 Consumer<Collection<KeyStatsDto>> resultConsumer) {
        resultConsumer.accept(getKeyStatsInner(symbols));
    }

    private List<KeyStatsDto> getKeyStatsInner(Collection<String> symbols) {
        Map<String, Quote> quotesMap = symbols.stream()
                .map(this::executeQuoteRequest)
                .collect(toMap(Quote::getSymbol, identity()));
        return symbols.stream()
                .map(this::executeKeyStatsRequest)
                .map(keyStats -> converter.convert(keyStats, quotesMap.get(keyStats.getSymbol())))
                .collect(toList());
    }

    private KeyStats executeKeyStatsRequest(String symbol) {
        RestRequest<KeyStats> request = new KeyStatsRequestBuilder()
                .withSymbol(symbol)
                .build();

        KeyStats stats = tradingClient.executeRequest(request);
        log.trace("Incoming stats request {}", stats);
        return stats;
    }

    private List<Chart> executeChartRequest(String symbol, ChartRange chartRange) {
        RestRequest<List<Chart>> request = new ChartRequestBuilder()
                .withSymbol(symbol)
                .withChartRange(chartRange)
                .build();

        List<Chart> charts = tradingClient.executeRequest(request);
        log.trace("Incoming charts request {}", charts);
        return charts;
    }

    private Quote executeQuoteRequest(String symbol) {
        RestRequest<Quote> quoteRestRequest = new QuoteRequestBuilder()
                .withSymbol(symbol)
                .build();

        Quote quote = tradingClient.executeRequest(quoteRestRequest);
        log.trace("Incoming quote {}", quote);
        return quote;
    }
    private List<LastTrade> executeLastRequest(Collection<String> symbols) {
        val lastTradeRequestBuilder = new LastTradeRequestBuilder();
        symbols.forEach(lastTradeRequestBuilder::withSymbol);

        List<LastTrade> quote = tradingClient.executeRequest(lastTradeRequestBuilder.build());
        log.trace("Incoming LastTrade {}", quote);
        return quote;
    }
}
