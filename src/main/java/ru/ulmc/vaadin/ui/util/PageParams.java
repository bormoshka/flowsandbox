package ru.ulmc.vaadin.ui.util;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ru.ulmc.vaadin.user.Permission;

/**
 * Статические параметры страницы
 */
@Getter
@Builder
public class PageParams {
    private final String description;
    private final Permission readPermission;
    private final Permission writePermission;

    public static PageParamsBuilder from(Permission readPermission) {
        return PageParams.builder().readPermission(readPermission);
    }

    public static PageParamsBuilder from(Permission readPermission, Permission writePermission) {
        return from(readPermission).writePermission(writePermission);
    }
}
