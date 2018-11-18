package ru.ulmc.investor.data.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Currency {
    USD("$"),
    EUR("€"),
    RUB("\u20BD");

    private final String specialChar;

    @Override
    public String toString() {
        return specialChar;
    }
}
