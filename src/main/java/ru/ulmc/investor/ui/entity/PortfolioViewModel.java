package ru.ulmc.investor.ui.entity;

import lombok.*;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.data.entity.Portfolio;
import ru.ulmc.investor.data.entity.Position;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.ThreadLocal.withInitial;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.hibernate.Hibernate.isInitialized;
import static ru.ulmc.investor.ui.util.Format.BIG_DECIMAL_SHORT;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class PortfolioViewModel implements Serializable {
    private Long id;
    private String name;
    private List<PositionViewModel> positions;
    @Getter
    private Map<Currency, PositionsStat> perCurrencyPositionStats;

    PortfolioViewModel(Portfolio portfolio) {
        id = portfolio.getId();
        name = portfolio.getName();
        if (isInitialized(portfolio.getPositions())) {
            this.perCurrencyPositionStats = portfolio.getPositions().stream()
                    .map(PositionsStat::of)
                    .collect(groupingBy(PositionsStat::getCurrency)).entrySet().stream()
                    .map(entry -> entry.getValue().stream()
                            .reduce(PositionsStat::add))
                    .map(Optional::get)
                    .collect(toMap(PositionsStat::getCurrency, positionsStat -> positionsStat));
        } else {
            positions = Collections.emptyList();
        }
    }

    public static PortfolioViewModel of(Portfolio portfolio) {
        return new PortfolioViewModel(portfolio);
    }

    public static Portfolio toEntity(PortfolioViewModel portfolio) {
        return new Portfolio(portfolio.getId(), portfolio.getName());
    }

    public int getPositionsTotal() {
        return positions.size();
    }

    public String getTotalInvestedValue() {
        return BIG_DECIMAL_SHORT.get().format(getPositions().stream()
                .mapToDouble(bp -> bp.getOpenPrice()
                        .multiply(bp.getCurrencyOpenPrice())
                        .multiply(BigDecimal.valueOf(bp.getQuantity()))
                        .doubleValue()) //hmmmm...
                .sum());
    }

    @Getter
    public static class PositionsStat {
        private Currency currency;

        private BigDecimal openPositionSum = BigDecimal.ZERO;
        private Integer openPositionCount = 1;

        private BigDecimal closedPositionSum = BigDecimal.ZERO;
        private Integer closedPositionCount = 0;

        private BigDecimal profit = BigDecimal.ZERO;

        private static PositionsStat of(Position pos) {
            PositionsStat ps = new PositionsStat();
            ps.currency = pos.getInstrument().getCurrency();
            if (pos.getClosed()) {
                ps.closedPositionSum = pos.getClosePrice()
                        .multiply(BigDecimal.valueOf(pos.getQuantity()));
                ps.closedPositionCount = 1;
                ps.openPositionCount = 0;
                BigDecimal openSum = pos.getOpenPrice()
                        .multiply(BigDecimal.valueOf(pos.getQuantity()));
                ps.profit = ps.closedPositionSum.subtract(openSum);
            } else {
                ps.openPositionSum = pos.getOpenPrice()
                        .multiply(BigDecimal.valueOf(pos.getQuantity()));
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
