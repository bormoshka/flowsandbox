package ru.ulmc.investor.ui.entity.position;

import lombok.*;
import ru.ulmc.investor.service.dto.KeyStatsDto;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PriceChange {
    public static final PriceChange empty = PriceChange.builder().build();
    private static final Value emptyValue = new Value();

    @Builder.Default
    private Value day = emptyValue;
    @Builder.Default
    private Value week = emptyValue;
    @Builder.Default
    private Value month = emptyValue;
    @Builder.Default
    private Value sixMonth = emptyValue;

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        private String changeVal;
        private String change;
        private boolean growth;
        private boolean present = false;

        public static Value from(KeyStatsDto.Value stat) {
            String percents = stat.getPercents().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            String changeVal = stat.getValueChange().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            return new Value(changeVal, percents, stat.getPercents().doubleValue() > 0, true);
        }
    }
}
