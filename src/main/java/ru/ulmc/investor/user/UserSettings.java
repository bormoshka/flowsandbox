package ru.ulmc.investor.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Пользовательские настройки
 */
@Getter
@Setter
@ToString
public class UserSettings {
    private boolean isHelloMessageRead;
    private boolean showSomeComponent = true;
    private String username;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}
