package ru.ulmc.investor.ui.entity;

import lombok.*;
import ru.ulmc.investor.data.entity.Position;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = {"id", "instrument"})
public class PositionViewModel {
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Long id;
    private String comment;
    private PortfolioLightModel portfolio;
    private InstrumentViewModel instrument;
    private int quantity;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private BigDecimal openPrice;
    private BigDecimal currencyOpenPrice;
    private BigDecimal closePrice;

    private BigDecimal currencyClosePrice;

    private boolean closed;

    public static PositionViewModel of(Position position) {
        return PositionViewModel.builder()
                .id(position.getId())
                .comment(position.getComment())
                .portfolio(PortfolioLightModel.of(position.getPortfolio()))
                .instrument(InstrumentViewModel.of(position.getInstrument()))
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
                .instrument(InstrumentViewModel.toEntity(position.getInstrument()))
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

    public String getStatus() {
        return closed ? "closed" : "open";
    }

    public String getProfitStatus() {
        if (closed) {
            BigDecimal openPrice = getOpenPrice();
            return openPrice.compareTo(getClosePrice()) > 0 ? "loss" : "profit";
        } else {
            return "still-open";
        }
    }

    public String getBroker() {
        return instrument.getBroker().getName();
    }

    public String getBaseCurrency() {
        return instrument.getCurrency().name();
    }

    public String getStockCode() {
        return instrument.getCode();
    }

    public String getStockName() {
        return instrument.getName();
    }

    public BigDecimal getProfitPercents() {
        if (!closed) {
            return BigDecimal.ZERO;
        }
        BigDecimal size = BigDecimal.valueOf(quantity);
        return getProfit().multiply(BigDecimal.valueOf(100))
                .divide(openPrice.multiply(size), 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getProfit() {
        if (!closed) {
            return BigDecimal.ZERO;
        }
        BigDecimal size = BigDecimal.valueOf(quantity);

        return closePrice.subtract(openPrice).multiply(size);
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
