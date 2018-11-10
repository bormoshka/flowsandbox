package ru.ulmc.investor.ui.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PositionStatusFilter {
    ALL("Все"),
    OPEN("Открытые"),
    CLOSED("Закрытые");
    private final String description;

}
