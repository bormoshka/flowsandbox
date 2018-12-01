package ru.ulmc.investor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.zankowski.iextrading4j.api.stocks.Company;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.data.entity.CompanyInfo;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.repository.CompanyRepository;
import ru.ulmc.investor.data.repository.LastPriceRepository;
import ru.ulmc.investor.data.repository.QuoteRepository;
import ru.ulmc.investor.service.convert.IEXMarketConverter;

@Slf4j
@Service
public class MarketService {

    private final LastPriceRepository lastPriceRepository;
    private final QuoteRepository repository;
    private final CompanyRepository companyRepository;
    private final ExternalMarketService extMarketService;
    private final IEXMarketConverter marketConverter;

    @Autowired
    public MarketService(LastPriceRepository lastPriceRepository,
                         QuoteRepository repository,
                         CompanyRepository companyRepository,
                         ExternalMarketService extMarketService,
                         IEXMarketConverter marketConverter) {
        this.lastPriceRepository = lastPriceRepository;
        this.repository = repository;
        this.companyRepository = companyRepository;
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
