package ru.ulmc.investor.ui.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import ru.ulmc.investor.data.entity.BasePosition;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.data.entity.Portfolio;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(of = "portfolio")
@ToString
public class PortfolioViewModel implements Serializable {
    @Delegate
    private final Portfolio portfolio;

    @Getter
    private Map<Currency, PositionsStat> perCurrencyPositionStats;

    public PortfolioViewModel(Portfolio portfolio) {
        this.portfolio = portfolio;
        this.perCurrencyPositionStats = getPositions().stream()
                .map(PositionsStat::of)
                .collect(Collectors.groupingBy(PositionsStat::getCurrency))
                .entrySet().stream()
                .map(entry -> entry.getValue().stream()
                        .reduce(PositionsStat::add))
                .map(Optional::get)
                .collect(Collectors.toMap(PositionsStat::getCurrency, positionsStat -> positionsStat));
    }

    public int getPositionsTotal() {
        return getPositions().size();
    }

    public String getTotalInvestedValue() {
        return String.valueOf(getPositions().stream()
                .mapToDouble(bp -> bp.getOpenPrice()
                        .multiply(bp.getCurrencyOpenPrice())
                        .multiply(BigDecimal.valueOf(bp.getSize()))
                        .doubleValue()) //hmmmm...
                .sum()); //todo: format
    }

    @Getter
    public static class PositionsStat {
        private Currency currency;

        private BigDecimal openPositionSum = BigDecimal.ZERO;
        private Integer openPositionCount = 1;

        private BigDecimal closedPositionSum = BigDecimal.ZERO;
        private Integer closedPositionCount = 0;

        private BigDecimal profit = BigDecimal.ZERO;

        private static PositionsStat of(BasePosition pos) {
            PositionsStat ps = new PositionsStat();
            ps.currency = pos.getPositionCurrency();
            if (pos.getClosed()) {
                ps.closedPositionSum = pos.getClosePrice()
                        .multiply(BigDecimal.valueOf(pos.getSize()));
                ps.closedPositionCount = 1;
                ps.openPositionCount = 0;
                BigDecimal openSum = pos.getOpenPrice()
                        .multiply(BigDecimal.valueOf(pos.getSize()));
                ps.profit = ps.closedPositionSum.subtract(openSum);
            } else {
                ps.openPositionSum = pos.getOpenPrice()
                        .multiply(BigDecimal.valueOf(pos.getSize()));
            }
            return ps;
        }

        private PositionsStat add(PositionsStat toAdd) {
            openPositionSum = openPositionSum.add(toAdd.openPositionSum);
            closedPositionSum = closedPositionSum.add(toAdd.closedPositionSum);
            profit = profit.add(toAdd.profit);
            openPositionCount += toAdd.openPositionCount;
            closedPositionCount += toAdd.closedPositionCount;
            return this;
        }
    }

}
