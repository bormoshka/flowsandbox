package ru.ulmc.investor.ui.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfitStatus {
    NEUTRAL("neutral"),
    PROFIT("profit"),
    LOSS("loss");
    private final String desc;
}
