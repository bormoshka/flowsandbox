package ru.ulmc.vaadin.user;

import lombok.Getter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum Permission {
    FRONT_PAGE_READ(Action.READ, "Право на открытие главной страницы"),
    DASHBOARD_READ(Action.READ, "Право на открытие дэшборда"),
    SETTINGS_READ(Action.READ, "Право на открытие страницы настроек"),
    INFORMATION_READ(Action.READ, "Право на открытие страницы настроек"),
    SETTINGS_WRITE(Action.WRITE, "Право на изменение параметров на странице настроек"),
    MONITORING_READ(Action.READ, "Право на открытие средств мониторинга"),
    MONITORING_WRITE(Action.WRITE, "Право на изменение настроек средств мониторинга"),
    ;

    private final Action action;
    private final String description;

    Permission(Action action, String description) {
        this.action = action;
        this.description = description;
    }

    public static Permission[] readAll() {
        return Stream.of(Permission.values())
                .filter(permission -> permission.action == Action.READ)
                .collect(Collectors.toList())
                .toArray(new Permission[]{});
    }

    public static Permission[] all() {
        return Permission.values();
    }

    public enum Action {
        READ,
        WRITE,
        OTHER;
    }
}
