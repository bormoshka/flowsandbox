package ru.ulmc.vaadin.user;

import lombok.Getter;

@Getter
public class CurrentUser {
    private UserRole role;
    private UserSettings userSettings;

    public CurrentUser(UserRole role) {
        this.role = role;
        userSettings = new UserSettings();
    }
}
