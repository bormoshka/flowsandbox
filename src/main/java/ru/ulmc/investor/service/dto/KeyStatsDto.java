package ru.ulmc.investor.service.dto;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyStatsDto implements Serializable {
    private String symbol;
    private LocalDate requestDate;
    private Value week;
    private Value month;
    private Value sixMonth;
    private Value year;
    private Value day;

    public static Value toKeyStatsValue(BigDecimal percents, BigDecimal fullPrice) {
        return Value.of(percents, fullPrice.multiply(percents));
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
