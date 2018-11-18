package ru.ulmc.investor.ui.entity;

import com.vaadin.flow.templatemodel.TemplateModel;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.data.entity.Position;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static ru.ulmc.investor.ui.entity.ProfitStatus.*;

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
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private BigDecimal openPrice;
    private BigDecimal currencyOpenPrice;
    private BigDecimal closePrice;
    private BigDecimal currencyClosePrice;

    private BigDecimal marketPrice;
    private boolean closed;

    public static PositionViewModel of(Pair<Position, Optional<LastPrice>> positionToLastPrice) {
        PositionViewModel model = of(positionToLastPrice.getKey());
        positionToLastPrice.getValue().ifPresent(lp -> model.setMarketPrice(lp.getLastPrice()));
        return model;
    }

    public static PositionViewModel of(Position position) {
        return PositionViewModel.builder()
                .id(position.getId())
                .comment(position.getComment())
                .portfolio(PortfolioLightModel.of(position.getPortfolio()))
                .symbol(SymbolViewModel.of(position.getSymbol()))
                .quantity(position.getQuantity())
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

    @NoArgsConstructor
    public static class PositionTotal extends PositionPrice {
        public PositionTotal(PositionViewModel model) {
            super(model);
        }

        @Override
        public BigDecimal getOpen() {
            return model.getOpenPrice().multiply(getSize());
        }

        @Override
        public BigDecimal getClose() {
            if (model.getClosePrice() == null) {
                return null;
            }
            return model.getClosePrice().multiply(getSize());
        }

        @Override
        public BigDecimal getMarket() {
            if (isLastPriceInitialized()) {
                return model.marketPrice.multiply(getSize());
            }
            return ZERO;
        }

    }

    @NoArgsConstructor
    public static class PositionPrice implements TemplateModel {
        PositionViewModel model;

        public PositionPrice(PositionViewModel model) {
            this.model = model;
        }

        public String getBaseCurrency() {
            return model.getBaseCurrency();
        }

        public boolean isClosed() {
            return model.isClosed();
        }

        public boolean isLastPriceInitialized() {
            return model.marketPrice != null;
        }

        public boolean isOpenWithMarket() {
            return !isClosed() && isLastPriceInitialized();
        }

        public BigDecimal getOpen() {
            return model.getOpenPrice();
        }

        public BigDecimal getClose() {
            return model.getClosePrice();
        }

        public BigDecimal getMarket() {
            return model.marketPrice;
        }

        public BigDecimal getProfitPercents() {
            if (isClosed() || isLastPriceInitialized()) {
                return getProfit().multiply(valueOf(100))
                        .divide(this.getOpen().multiply(getSize()), 2, BigDecimal.ROUND_HALF_UP);
            } else {
                return ZERO;
            }

        }

        BigDecimal getSize() {
            return valueOf(model.quantity);
        }

        public BigDecimal getProfit() {
            if (isClosed()) {
                return getClose().subtract(getOpen())/*.multiply(getSize())*/;
            } else if (isLastPriceInitialized()) {
                return getMarket().subtract(getOpen())/*.multiply(getSize())*/;
            } else {
                return ZERO;
            }
        }

        public boolean isProfitable() {
            if (isClosed()) {
                return getOpen().compareTo(getClose()) < 0;
            } else if (isLastPriceInitialized()) {
                return getOpen().compareTo(getMarket()) < 0;
            } else {
                return false;
            }
        }

        public String getTrending() {
            return isProfitable() ? "up" : "down";
        }

       /* public String getProfitStatus() {
            if (isClosed() || isOpenWithMarket()) {
                return isProfitable() ? LOSS.getDesc() : PROFIT.getDesc();
            } else {
                return NEUTRAL.getDesc();
            }
        }*/
    }
}
