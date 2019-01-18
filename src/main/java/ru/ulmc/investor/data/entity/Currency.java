package ru.ulmc.investor.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Currency {
    USD("$"),
    EUR("€"),
    RUB("\u20BD");
    @Getter
    private final String specialChar;

    @Override
    public String toString() {
        return specialChar;
    }
}
