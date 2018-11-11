package ru.ulmc.investor.ui.entity;

import com.vaadin.flow.templatemodel.Encode;
import com.vaadin.flow.templatemodel.TemplateModel;
import lombok.*;
import ru.ulmc.investor.ui.util.encoder.BigDecimapEncoder;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PositionResultViewModel implements TemplateModel {
    private String baseCurrencyResult;
    private String baseCurrency;
    private List<PerCurrencyResult> perCurrencyResults;

    @Getter
    @ToString
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerCurrencyResult implements TemplateModel, Comparable<PerCurrencyResult> {
        @Builder.Default
        private BigDecimal totalInvested = ZERO;
        @Builder.Default
        private BigDecimal totalProfit = ZERO;
        private String currency;
        @Builder.Default
        private boolean hasAnyClosedPositions = false;
        private boolean loss;

        public PerCurrencyResult recalc() {
            loss = totalProfit.compareTo(ZERO) < 0;
            return this;
        }
        @Override
        public int compareTo(PerCurrencyResult o) {
            return currency.compareTo(o.getCurrency());
        }

        @Encode(BigDecimapEncoder.class)
        public BigDecimal getTotalInvested() {
            return totalInvested;
        }

        @Encode(BigDecimapEncoder.class)
        public BigDecimal getTotalProfit() {
            return totalProfit;
        }
    }
}
