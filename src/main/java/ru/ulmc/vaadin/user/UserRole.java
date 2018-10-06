package ru.ulmc.vaadin.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public enum UserRole {
    ANONYMOUS("Пользователь без прав доступа"),
    USER("Рядовой пользователь системы", Permission.readAll()),
    ADMIN("Администратор системы", Permission.all()),
    PING_USER("Проверка работоспособности через http-запрос", Permission.FRONT_PAGE_READ);

    private final Set<Permission> permissions;
    private final String description;

    UserRole(String description, Permission... perms) {
        this.permissions = new HashSet<>(Arrays.asList(perms));
        this.description = description;
    }

    public boolean hasPermission(Permission perm) {
        return permissions.contains(perm);
    }

    public boolean hasAnyPermission(Permission... perms) {
        return Stream.of(perms).anyMatch(permissions::contains);
    }

    public boolean hasAllPermission(Permission... perms) {
        return Stream.of(perms).allMatch(permissions::contains);
    }
}
