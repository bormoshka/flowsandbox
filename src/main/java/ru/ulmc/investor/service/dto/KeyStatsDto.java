package ru.ulmc.investor.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyStatsDto implements Serializable {
    public static final BigDecimal BD_100 = BigDecimal.valueOf(100);
    private String symbol;
    private LocalDate requestDate;
    private Value week;
    private Value month;
    private Value sixMonth;
    private Value year;
    private Value day;

    public static Value toKeyStatsValue(BigDecimal percents, BigDecimal currentPrice) {
        BigDecimal percents100 = BD_100.multiply(percents);
        return Value.of(percents100, getChangedValue(percents100, currentPrice));
    }

    private static BigDecimal getChangedValue(BigDecimal percents, BigDecimal currentPrice) {
        BigDecimal percentsFromOrigin = BD_100.add(percents).abs();
        BigDecimal originalValue = currentPrice.multiply(BD_100).divide(percentsFromOrigin, RoundingMode.HALF_UP);
        return currentPrice.subtract(originalValue);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value implements Serializable {
        private BigDecimal percents;
        private BigDecimal valueChange;

        public static Value of(BigDecimal percents, BigDecimal valueChange) {
            return new Value(percents, valueChange);
        }
    }

}
