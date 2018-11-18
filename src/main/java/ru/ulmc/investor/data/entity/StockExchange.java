package ru.ulmc.investor.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockExchange {
    NASDAQ("NASDAQ"),
    NYSE("NYSE"),
    MCX("Московская биржа"),
    UNKNOWN("Не известно");

    private final String name;
}
