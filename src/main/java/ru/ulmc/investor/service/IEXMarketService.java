package ru.ulmc.investor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import pl.zankowski.iextrading4j.api.exception.IEXTradingException;
import pl.zankowski.iextrading4j.api.marketdata.LastTrade;
import pl.zankowski.iextrading4j.api.stocks.Company;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.manager.RestRequest;
import pl.zankowski.iextrading4j.client.rest.request.stocks.CompanyRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;
import pl.zankowski.iextrading4j.client.socket.request.marketdata.LastAsyncRequestBuilder;
import ru.ulmc.investor.data.entity.InnerQuote;
import ru.ulmc.investor.service.convert.IEXMarketConverter;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Slf4j
@Service
public class IEXMarketService {
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
    public Future<InnerQuote> getQuoteAsync(String symbol) {
        Quote quote = executeQuoteRequest(symbol);
        return new AsyncResult<>(converter.convert(quote));
    }

    @Async
    public void getQuoteAsync(String symbol, Consumer<InnerQuote> quoteConsumer) {
        Quote quote = executeQuoteRequest(symbol);
        quoteConsumer.accept(converter.convert(quote));
    }

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

    private Quote executeQuoteRequest(String symbol) {
        RestRequest<Quote> quoteRestRequest = new QuoteRequestBuilder()
                .withSymbol(symbol)
                .build();

        Quote quote = tradingClient.executeRequest(quoteRestRequest);
        log.trace("Incoming quote {}", quote);
        return quote;
    }

    /**
     * Подписывается на изменения по инструментам.
     *
     * @param symbols Набор инструментов для подписания
     * @param lastTradeConsumer потребитель обновлений
     */
    public void subscribeForLastTrade(Collection<String> symbols,
                                      Consumer<LastTrade> lastTradeConsumer) {
        LastAsyncRequestBuilder requestBuilder = new LastAsyncRequestBuilder();
        symbols.forEach(requestBuilder::withSymbol);

        tradingClient.subscribe(requestBuilder.build(), lastTradeConsumer);
    }
}
