package ru.ulmc.investor.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class KeyStatsDto implements Serializable {
    private BigDecimal weekChangePercents;
    private BigDecimal monthChangePercents;
    private BigDecimal sixMonthChangePercents;
    private BigDecimal yearChangePercents;
}
