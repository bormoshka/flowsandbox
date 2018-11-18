package ru.ulmc.investor.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SymbolType {
    IRRELEVANT("Не важно"),
    STOCK("Акции"),
    SIMPLE_BONDS("Простые облигации"),
    ETF("ETF");

    private final String description;

}
