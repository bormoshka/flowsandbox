package ru.ulmc.investor.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.zankowski.iextrading4j.api.marketdata.LastTrade;
import pl.zankowski.iextrading4j.api.stocks.Company;
import ru.ulmc.investor.data.entity.CompanyInfo;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.repository.CompanyRepository;
import ru.ulmc.investor.data.repository.LastPriceRepository;
import ru.ulmc.investor.data.repository.QuoteRepository;
import ru.ulmc.investor.service.convert.IEXMarketConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@Service
public class MarketService {

    private final LastPriceRepository lastPriceRepository;
    private final QuoteRepository repository;
    private final CompanyRepository companyRepository;
    private final IEXMarketService extMarketService;
    private final IEXMarketConverter marketConverter;

    @Autowired
    public MarketService(LastPriceRepository lastPriceRepository,
                         QuoteRepository repository,
                         CompanyRepository companyRepository,
                         IEXMarketService extMarketService,
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

    private void handleNewLastPrice(Consumer<LastPrice> quoteConsumer, LastTrade lastTrade) {
        LastPrice lp = marketConverter.convert(lastTrade);
        saveIfNotPresent(lp);
        quoteConsumer.accept(lp);
    }


    private void saveIfNotPresent(LastPrice lp) {
        LastPrice cached = lastPriceRepository.findFirstBySymbolAndDateTime(lp.getSymbol(), lp.getDateTime());
        if (cached == null) {
            lastPriceRepository.save(lp);
        }
    }
}
