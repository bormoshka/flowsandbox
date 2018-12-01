package ru.ulmc.investor.event.dto;

public interface BaseEvent<T> {
    T getData();
}
