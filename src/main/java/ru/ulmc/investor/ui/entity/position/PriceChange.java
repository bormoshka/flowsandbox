package ru.ulmc.investor.ui.entity.position;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@Builder
public class PriceChange {
    public static final PriceChange empty = PriceChange.builder().present(false).build();

    private boolean present = true;
    private Value day;
    private Value week;
    private Value month;

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        private BigDecimal changeVal;
        private Double change;
        private boolean growth;

        public static Value from(BigDecimal changeVal, Double change) {
            return new Value(changeVal, change, change > 0);
        }
    }
}
