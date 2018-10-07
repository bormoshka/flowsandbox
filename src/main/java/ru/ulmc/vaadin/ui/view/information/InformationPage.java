package ru.ulmc.vaadin.ui.view.information;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ulmc.vaadin.service.UserService;
import ru.ulmc.vaadin.ui.MainLayout;
import ru.ulmc.vaadin.ui.util.PageParams;
import ru.ulmc.vaadin.ui.util.TopLevelPage;
import ru.ulmc.vaadin.ui.view.CommonPage;
import ru.ulmc.vaadin.user.Permission;

@SpringComponent
@UIScope
@TopLevelPage(menuName = "Справочник", order = 3)
@Route(value = "info", layout = MainLayout.class)
public class InformationPage extends CommonPage {
    public static final PageParams PAGE = PageParams.from(Permission.INFORMATION_READ).build();
    private final UserService userService;

    @Autowired
    public InformationPage(UserService userService) {
        super(userService, PAGE);
        this.userService = userService;
        layout.add(new Label("Info page"));
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу

    }
}
