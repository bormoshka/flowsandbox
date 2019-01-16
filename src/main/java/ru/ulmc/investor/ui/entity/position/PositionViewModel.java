package ru.ulmc.investor.ui.entity.position;

import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.entity.Position;
import ru.ulmc.investor.ui.entity.PortfolioLightModel;
import ru.ulmc.investor.ui.entity.SymbolViewModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode(of = {"id", "symbol"})
public class PositionViewModel {
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Long id;
    private String comment;
    private PortfolioLightModel portfolio;
    private SymbolViewModel symbol;
    private int quantity;
    private int quantityClosed;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private BigDecimal openPrice;
    private BigDecimal currencyOpenPrice;
    private BigDecimal closePrice;
    private BigDecimal currencyClosePrice;

    private BigDecimal marketPrice;
    private boolean closed;
    @Builder.Default
    private boolean parent = false;

    public static PositionViewModel of(Pair<Position, Optional<LastPrice>> positionToLastPrice) {
        PositionViewModel model = of(positionToLastPrice.getKey());
        positionToLastPrice.getValue().ifPresent(lp -> model.setMarketPrice(lp.getLastPrice()));
        return model;
    }

    public static PositionViewModel makeParentFrom(@NonNull Collection<PositionViewModel> children) {
        if(children.isEmpty()) {
            throw new IllegalArgumentException("Children cannot be empty!");
        }
        PositionViewModel firstChild = null;
        BigDecimal midOpenPrice = ZERO;
        BigDecimal midClosePrice = ZERO;
        int totalQuantity = 0;
        int closedQuantity = 0;
        int midCount = 0;
        int midCloseCount = 0;
        for (PositionViewModel child : children) {
            if (firstChild == null) {
                firstChild = child;
            }
            if (child.isClosed()) {
                closedQuantity += child.getQuantityClosed();
                midClosePrice = calcMid(midClosePrice, child.getClosePrice(), ++midCloseCount);
            }
            totalQuantity += child.getQuantity();
            midOpenPrice = calcMid(midOpenPrice, child.getOpenPrice(), ++midCount);
        }
        return PositionViewModel.builder()
                .id(-1L)
                .comment("Parent node")
                .portfolio(firstChild.getPortfolio())
                .symbol(firstChild.getSymbol())
                .quantity(totalQuantity)
                .quantityClosed(closedQuantity)
                .openDate(firstChild.getOpenDate())
                .closeDate(firstChild.getCloseDate())
                .openPrice(midOpenPrice)
                .closePrice(midClosePrice)
                .currencyOpenPrice(ZERO)
                .closed(totalQuantity == closedQuantity)
                .parent(true)
                .build();
    }

    private static BigDecimal calcMid(BigDecimal midOpenPrice, BigDecimal nextPrice, int val) {
        BigDecimal right = nextPrice.subtract(midOpenPrice).divide(valueOf(val), RoundingMode.HALF_UP);
        return midOpenPrice.add(right);
    }

    public static PositionViewModel of(Position position) {
        return PositionViewModel.builder()
                .id(position.getId())
                .comment(position.getComment())
                .portfolio(PortfolioLightModel.of(position.getPortfolio()))
                .symbol(SymbolViewModel.of(position.getSymbol()))
                .quantity(position.getQuantity())
                .quantityClosed(position.getCloseDate() != null ? position.getQuantity() : 0)
                .openDate(position.getOpenDate())
                .closeDate(position.getCloseDate())
                .openPrice(position.getOpenPrice())
                .closePrice(position.getClosePrice())
                .currencyOpenPrice(position.getCurrencyOpenPrice())
                .currencyClosePrice(position.getCurrencyClosePrice())
                .closed(position.getClosed())
                .build();
    }

    public static Position toEntity(PositionViewModel position) {
        return Position.builder()
                .id(position.getId())
                .comment(position.getComment())
                .portfolio(PortfolioLightModel.toEntity(position.getPortfolio()))
                .symbol(SymbolViewModel.toEntity(position.getSymbol()))
                .quantity(position.getQuantity())
                .openDate(position.getOpenDate())
                .closeDate(position.getCloseDate())
                .openPrice(position.getOpenPrice())
                .closePrice(position.getClosePrice())
                .currencyOpenPrice(position.getCurrencyOpenPrice())
                .currencyClosePrice(position.getCurrencyClosePrice())
                .closed(position.isClosed())
                .build();
    }

    public PositionTotal getTotals() {
        return new PositionTotal(this);
    }

    public PositionPrice getPrices() {
        return new PositionPrice(this);
    }

    public Optional<BigDecimal> getMarketPrice() {
        return Optional.ofNullable(marketPrice);
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public String getStatus() {
        return closed ? "closed" : "open";
    }

    public String getBroker() {
        return symbol.getBroker().getName();
    }

    public String getBaseCurrency() {
        return symbol.getCurrency().name();
    }

    public String getStockCode() {
        return symbol.getCode();
    }

    public String getStockName() {
        return symbol.getName();
    }

    public BigDecimal getInvestedSummary() {
        return openPrice.multiply(valueOf(quantity));
    }

    public String getOpenDateFormatted() {
        return df.format(openDate);
    }

    public String getCloseDateFormatted() {
        return closeDate == null ? null : df.format(closeDate);
    }


    public String getOpenPeriod() {
        return closeDate == null ? "0" : getOpenPeriodUnsafe();
    }

    /**
     * Приблизительное время в часах/днях/месяцах/годах позиции перед закрытием
     */
    private String getOpenPeriodUnsafe() {
        Duration between = Duration.between(openDate, closeDate);
        long days = between.toDays();
        if (days > 30) {
            long years = days % 365;
            if (years >= 1) {
                return years + " л.";
            } else {
                return days % 30 + " м.";
            }
        } else if (days < 2) {
            return between.toHours() + " ч.";
        } else {
            return days + " д.";
        }
    }
}
