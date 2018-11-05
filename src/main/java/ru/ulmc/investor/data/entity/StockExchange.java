package ru.ulmc.investor.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockExchange {
    NASDAQ("NASDAQ"),
    NYSE("NYSE"),
    MCX("Моссковская биржа");

    private final String name;
}
