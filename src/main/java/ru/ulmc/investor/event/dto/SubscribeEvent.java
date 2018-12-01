package ru.ulmc.investor.event.dto;

import lombok.Value;

import java.util.Set;

@Value
public class SubscribeEvent {
    private final Set<String> symbols;
}
