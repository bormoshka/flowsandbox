package ru.ulmc.investor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.zankowski.iextrading4j.api.stocks.Company;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.ulmc.investor.data.entity.CompanyInfo;
import ru.ulmc.investor.data.entity.HistoryPrice;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.repository.CompanyRepository;
import ru.ulmc.investor.data.repository.HistoryPriceRepository;
import ru.ulmc.investor.data.repository.LastPriceRepository;
import ru.ulmc.investor.data.repository.QuoteRepository;
import ru.ulmc.investor.service.convert.IEXMarketConverter;
import ru.ulmc.investor.service.dto.KeyStatsDto;
import ru.ulmc.investor.ui.entity.position.PositionViewModel;
import ru.ulmc.investor.ui.entity.position.PriceChange;

@Slf4j
@Service
public class MarketService {

    private final LastPriceRepository lastPriceRepository;
    private final QuoteRepository repository;
    private final CompanyRepository companyRepository;
    private final HistoryPriceRepository historyPriceRepository;
    private final ExternalMarketService extMarketService;
    private final IEXMarketConverter marketConverter;

    @Autowired
    public MarketService(LastPriceRepository lastPriceRepository,
                         QuoteRepository repository,
                         CompanyRepository companyRepository,
                         HistoryPriceRepository historyPriceRepository,
                         ExternalMarketService extMarketService,
                         IEXMarketConverter marketConverter) {
        this.lastPriceRepository = lastPriceRepository;
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.historyPriceRepository = historyPriceRepository;
        this.extMarketService = extMarketService;
        this.marketConverter = marketConverter;
    }

    public Optional<CompanyInfo> getCompanyInfo(@NonNull String symbol) {
        String trimmedSymbol = symbol.trim();
        if (trimmedSymbol.isEmpty()) {
            return Optional.empty();
        }
        CompanyInfo firstBySymbol = companyRepository.findFirstBySymbol(trimmedSymbol);
        if (firstBySymbol == null) {
            Optional<Company> response = extMarketService.getCompany(symbol);
            if (response.isPresent()) {
                CompanyInfo convert = marketConverter.convert(response.get());
                companyRepository.save(convert);
                return Optional.of(convert);
            }
        }
        return Optional.empty();
    }

    public Collection<CompanyInfo> getCachedCompanies(String symbolSubString) {
        return companyRepository.findTop30BySymbolContainingOrderBySymbol(symbolSubString);
    }

    public void getLastPricesAsync(Collection<String> symbol, Consumer<LastPrice> quoteConsumer) {
        extMarketService.subscribeForLastTrade(symbol, lastTrade -> {
            log.trace("Getting last prices async {}", lastTrade);
            handleNewLastPrice(quoteConsumer, lastTrade);
        });
    }

    public void getBatchLastPricesAsync(Collection<String> symbol,
                                        Consumer<Collection<LastPrice>> quoteConsumer) {
        extMarketService.getLastPriceAsync(symbol, lastTrade -> {
            log.trace("Getting last prices async {}", lastTrade);
            handleNewLastPrices(quoteConsumer, lastTrade);
        });
    }

    public Collection<LastPrice> getBatchLastPrices(Collection<String> symbol) {
        Collection<LastPrice> lastPrices = extMarketService.getLastPrice(symbol);
        lastPrices.forEach(this::saveIfNotPresent);
        return lastPrices;
    }

    public void getKeyStats(Map<String, Collection<PositionViewModel>> positions) {
        extMarketService.getKeyStats(positions.keySet())
                .forEach(stat -> mapKeyStatsToViewModel(stat, positions.get(stat.getSymbol())));
    }

    private void mapKeyStatsToViewModel(KeyStatsDto stat, Collection<PositionViewModel> models) {
        for (PositionViewModel model : models) {
            Optional<BigDecimal> optMarketPrice = model.getMarketPrice();
            if (!optMarketPrice.isPresent()) {
                continue;
            }
            val day = PriceChange.Value.from(stat.getDay());
            val week = PriceChange.Value.from(stat.getWeek());
            val month = PriceChange.Value.from(stat.getMonth());
            model.setPriceChange(model.getPriceChange().toBuilder()
                    .present(true)
                    .day(day)
                    .week(week)
                    .month(month)
                    .build());
        }
    }

    private Optional<HistoryPrice> findHistoryPrices(LocalDate date, String symbol) {
        return historyPriceRepository.findById(HistoryPrice.HistoryPriceId.builder()
                .date(date)
                .symbol(symbol)
                .build());
    }

    private void handleNewLastPrice(Consumer<LastPrice> quoteConsumer, LastPrice lp) {
        saveIfNotPresent(lp);
        quoteConsumer.accept(lp);
    }

    private void handleNewLastPrices(Consumer<Collection<LastPrice>> quoteConsumer,
                                     Collection<LastPrice> lastTrades) {
        lastTrades.forEach(this::saveIfNotPresent);
        quoteConsumer.accept(lastTrades);
    }

    private void saveIfNotPresent(LastPrice lp) {
        LastPrice cached = lastPriceRepository.findFirstBySymbolAndDateTime(lp.getSymbol(), lp.getDateTime());
        if (cached == null) {
            lastPriceRepository.save(lp);
        }
    }
}
