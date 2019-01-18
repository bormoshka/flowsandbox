package ru.ulmc.investor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import pl.zankowski.iextrading4j.api.stocks.Company;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.data.entity.HistoryPrice;
import ru.ulmc.investor.data.entity.InnerQuote;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.repository.LastPriceRepository;
import ru.ulmc.investor.service.dto.KeyStatsDto;

import static ru.ulmc.investor.service.dto.KeyStatsDto.toKeyStatsValue;

@Slf4j
@Service
@Profile("random-sources")
public class RandomMarketService implements ExternalMarketService {
    public static final float VOLATILITY = 0.2f;
    public static final float AMOUNT_MULTIPLIER = 1.2f;
    private final Map<String, LastPrice> prices = new ConcurrentHashMap<>();
    private final LastPriceRepository repository;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public RandomMarketService(LastPriceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void getLastPriceAsync(Collection<String> symbols,
                                  Consumer<Collection<LastPrice>> quoteConsumer) {
        quoteConsumer.accept(getLastPricesInner(symbols));
    }

    @Override
    public Collection<LastPrice> getLastPrice(Collection<String> symbols) {
        return getLastPricesInner(symbols);
    }

    @Override
    public Future<InnerQuote> getQuoteAsync(String symbol) {
        throw new UnsupportedOperationException("this operation is not implemented yet");
    }

    @Override
    public void getQuoteAsync(String symbol, Consumer<InnerQuote> quoteConsumer) {
        throw new UnsupportedOperationException("this operation is not implemented yet");
    }

    @Override
    public Optional<Company> getCompany(String symbol) {
        return Optional.empty();
    }

    @Override
    public void subscribeForLastTrade(Collection<String> symbols,
                                      Consumer<LastPrice> lastTradeConsumer) {
        throw new UnsupportedOperationException("this operation is not implemented yet");
    }

    @Override
    public void getLastMonthHistoryData(String symbol,
                                        Consumer<Collection<HistoryPrice>> priceConsumer) {

    }

    @Override
    public List<KeyStatsDto> getKeyStats(Collection<String> symbols) {
        return symbols.stream().map(
                symbol -> KeyStatsDto.builder()
                        .symbol(symbol)
                        .requestDate(LocalDate.now())
                        .week(getStatValue(symbol))
                        .month(getStatValue(symbol))
                        .sixMonth(getStatValue(symbol))
                        .year(getStatValue(symbol))
                        .day(getStatValue(symbol))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void getKeyStatsAsync(Collection<String> symbols,
                                 Consumer<Collection<KeyStatsDto>> resultConsumer) {
        resultConsumer.accept(getKeyStats(symbols));
    }

    private KeyStatsDto.Value getStatValue(String symbol) {
        BigDecimal percentChange = BigDecimal.valueOf(random.nextDouble());
        LastPrice lastPrice = prices.get(symbol);
        BigDecimal price;
        if (lastPrice != null) {
            price = lastPrice.getLastPrice();
        } else {
            price = BigDecimal.TEN;
        }
        return toKeyStatsValue(percentChange, price);
    }

    private Set<LastPrice> getLastPricesInner(Collection<String> symbols) {
        return symbols.stream()
                .map(symbol -> updatePrice(this.prices.computeIfAbsent(symbol, this::getSavedPrice)))
                .collect(Collectors.toSet());
    }

    private LastPrice updatePrice(LastPrice lp) {
        return lp.toBuilder()
                .lastPrice(newPrice(lp.getLastPrice()))
                .dateTime(LocalDateTime.now())
                .build();
    }

    private BigDecimal newPrice(BigDecimal oldPrice) {
        float rand = random.nextFloat() - 0.5f;
        float change = AMOUNT_MULTIPLIER * VOLATILITY * rand;
        float old = oldPrice.floatValue();
        float amount = old + old * change;
        if (amount <= 0) {
            amount = 10 * random.nextFloat();
        }
        return BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private LastPrice getSavedPrice(String symbol) {
        LastPrice price = repository.findFirstBySymbolOrderByDateTimeDesc(symbol);
        if (price != null) {
            return price;
        } else {
            return getPrice(symbol);
        }
    }

    private LastPrice getPrice(String v) {

        return LastPrice.builder()
                .volume(BigDecimal.valueOf(1000))
                .dateTime(LocalDateTime.now())
                .id(UUID.randomUUID())
                .lastPrice(BigDecimal.valueOf(random.nextInt(1000)))
                .symbol(v)
                .build();
    }
}
