package ru.ulmc.vaadin.service;

import com.vaadin.flow.spring.annotation.SpringComponent;
import ru.ulmc.vaadin.user.CurrentUser;
import ru.ulmc.vaadin.user.UserRole;

/**
 * Любые данные связанные с пользователем
 */
@SpringComponent
public class UserService {

    private CurrentUser user = stub(); //todo: добавить нормальную авторизацию

    private static CurrentUser stub() {
        return new CurrentUser(UserRole.ADMIN);
    }

    public UserRole getCurrentUserRole() {
        return user.getRole();
    }

    public CurrentUser getCurrentUser() {
        return user;
    }
}
