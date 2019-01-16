package ru.ulmc.investor.ui.entity.position;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PriceChange {
    private BigDecimal changeValue;
    private boolean growth;
}
