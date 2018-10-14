package ru.ulmc.investor.ui.view.information;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.util.PageParams;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.view.CommonPage;
import ru.ulmc.investor.user.Permission;

@SpringComponent
@UIScope
@TopLevelPage(menuName = "Портфолио", order = 2)
@Route(value = "portfolio", layout = MainLayout.class)
public class PortfolioPage extends CommonPage {
    public static final PageParams PAGE = PageParams.from(Permission.INFORMATION_READ).build();
    private final UserService userService;

    @Autowired
    public PortfolioPage(UserService userService) {
        super(userService, PAGE);
        this.userService = userService;
        layout.add(new Label("Info page"));
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу данными

    }


}
