package ru.ulmc.vaadin.ui.view;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ulmc.vaadin.service.UserService;
import ru.ulmc.vaadin.ui.MainLayout;
import ru.ulmc.vaadin.ui.util.PageParams;

@SpringComponent
@UIScope
@Route(value = "403", layout = MainLayout.class)
public class Error403Page extends CommonPage {
    public static final PageParams PAGE = PageParams.builder().build();
    private final UserService userService;

    @Autowired
    public Error403Page(UserService userService) {
        super(userService, PAGE);
        this.userService = userService;
        layout.add(new Label("403! Forbidden!"));
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу
    }
}
