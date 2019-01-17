package ru.ulmc.investor.ui.entity.position;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.ulmc.investor.service.dto.KeyStatsDto;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PriceChange {
    public static final PriceChange empty = PriceChange.builder().present(false).build();
    private static final Value emptyValue = new Value();
    @Builder.Default
    private boolean present = true;
    @Builder.Default
    private Value day = emptyValue;
    @Builder.Default
    private Value week = emptyValue;
    @Builder.Default
    private Value month = emptyValue;

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        private BigDecimal changeVal;
        private Double change;
        private boolean growth;

        public static Value from(KeyStatsDto.Value stat) {
            double percents = stat.getPercents().doubleValue();
            return new Value(stat.getValueChange(), percents, percents > 0);
        }
    }
}
