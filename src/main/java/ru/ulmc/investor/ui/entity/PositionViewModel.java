package ru.ulmc.investor.ui.entity;

import lombok.*;
import ru.ulmc.investor.data.entity.Position;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = {"id", "stockPosition"})
public class PositionViewModel {
    private Long id;
    private String comment;
    private PortfolioLightModel portfolio;
    private StockViewModel stockPosition;
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
                .stockPosition(StockViewModel.of(position.getStockPosition()))
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
                .stockPosition(StockViewModel.toEntity(position.getStockPosition()))
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
        return getStockPosition().getBroker().getName();
    }

    public String getBaseCurrency() {
        return getStockPosition().getCurrency().name();
    }

    public String getStockCode() {
        return getStockPosition().getCode();
    }

    public String getStockName() {
        return getStockPosition().getName();
    }

    public BigDecimal getProfit() {
        if (!closed) {
            return BigDecimal.ZERO;
        }
        BigDecimal size = BigDecimal.valueOf(quantity);

        return closePrice.subtract(openPrice).multiply(size);
    }

    public BigDecimal getProfitPercents() {
        if (!closed) {
            return BigDecimal.ZERO;
        }
        BigDecimal size = BigDecimal.valueOf(quantity);
        return getProfit().multiply(BigDecimal.valueOf(100))
                .divide(openPrice.multiply(size), 4, BigDecimal.ROUND_HALF_UP);
    }


}
