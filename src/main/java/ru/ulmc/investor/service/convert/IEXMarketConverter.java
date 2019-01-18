package ru.ulmc.investor.service.convert;

import org.springframework.stereotype.Component;
import pl.zankowski.iextrading4j.api.marketdata.LastTrade;
import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.Company;
import pl.zankowski.iextrading4j.api.stocks.KeyStats;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import ru.ulmc.investor.data.entity.*;
import ru.ulmc.investor.data.entity.HistoryPrice.HistoryPriceId;
import ru.ulmc.investor.service.dto.KeyStatsDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static ru.ulmc.investor.service.dto.KeyStatsDto.toKeyStatsValue;

@Component
public class IEXMarketConverter implements MarketConverter {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public KeyStatsDto convert(KeyStats keyStats, Quote quote) {
        BigDecimal price = quote.getLatestPrice();
        return KeyStatsDto.builder()
                .symbol(keyStats.getSymbol())
                .requestDate(LocalDate.now())
                .week(toKeyStatsValue(keyStats.getDay5ChangePercent(), price))
                .month(toKeyStatsValue(keyStats.getMonth1ChangePercent(), price))
                .sixMonth(toKeyStatsValue(keyStats.getMonth6ChangePercent(), price))
                .year(toKeyStatsValue(keyStats.getYear1ChangePercent(), price))
                .day(KeyStatsDto.Value.of(quote.getChangePercent(), quote.getChange()))
                .build();
    }

    public List<HistoryPrice> convert(String symbol, List<Chart> charts) {
        return charts.stream()
                .map(chart -> getHistoryPrice(symbol, chart))
                .collect(Collectors.toList());
    }

    private HistoryPrice getHistoryPrice(String symbol, Chart chart) {
        LocalDate date = LocalDate.from(ISO_DATE_FORMATTER.parse(chart.getDate()));
        HistoryPriceId id = HistoryPriceId.builder()
                .date(date)
                .symbol(symbol)
                .build();
        return HistoryPrice.builder()
                .id(id)
                .close(chart.getClose())
                .volume(chart.getVolume())
                .build();
    }

    public InnerQuote convert(Quote quote) {
        return InnerQuote.builder()
                .symbol(quote.getSymbol())
                .date(getDate(quote.getOpenTime()))
                .openTime(getTime(quote.getOpenTime()))
                .closeTime(getTime(quote.getCloseTime()))
                .openPrice(quote.getOpen())
                .closePrice(quote.getClose())
                .build();
    }

    public LastPrice convert(LastTrade lastTrade) {
        return LastPrice.builder()
                .symbol(lastTrade.getSymbol())
                .dateTime(getDateTime(lastTrade.getTime()))
                .lastPrice(lastTrade.getPrice())
                .volume(lastTrade.getSize())
                .build();
    }

    public CompanyInfo convert(Company company) {
        return CompanyInfo.builder()
                .symbol(company.getSymbol())
                .name(company.getCompanyName())
                .description(company.getDescription())
                .ceo(company.getCEO())
                .industry(company.getIndustry())
                .sector(company.getSector())
                .stockExchange(getStock(company.getExchange()))
                .type(getType(company.getIssueType()))
                .build();
    }

    private SymbolType getType(String issueType) {
        if(issueType.equalsIgnoreCase("cs")) {
            return SymbolType.STOCK;
        } else if(issueType.equalsIgnoreCase("et")) {
            return SymbolType.ETF;
        }
        return SymbolType.IRRELEVANT;
    }

    private StockExchange getStock(String exchange) {
        if(exchange.contains("NASDAQ")) {
            return StockExchange.NASDAQ;
        }
        return StockExchange.UNKNOWN;
    }

    private LocalDate getDate(Long openTime) {
        return new Date(openTime).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime getTime(Long time) {
        return new Date(time).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private LocalDateTime getDateTime(Long time) {
        return new Date(time).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
