package ru.ulmc.investor.ui.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.util.PageParams;
import ru.ulmc.investor.user.Permission;
import ru.ulmc.investor.user.UserSettings;

@SpringComponent
@UIScope
@TopLevelPage(menuName = "Главная", order = 1)
@Route(value = "", layout = MainLayout.class)
public class MainPage extends CommonPage {
    public static final PageParams PAGE = PageParams.from(Permission.FRONT_PAGE_READ).build();

    @Autowired
    public MainPage(UserService userService) {
        super(userService, PAGE);
        layout.add(new Label("Hello!"));
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу

    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
       // UserSettings userSettings = userService.getCurrentUser().getUserSettings();
       // if (!userSettings.isHelloMessageRead()) {
       //     BeforeLeaveEvent.ContinueNavigationAction action = event.postpone();
       //     Dialog dialog = new Dialog();
       //     dialog.addDialogCloseActionListener(clsEvent -> {
       //         userSettings.setHelloMessageRead(true);
       //         dialog.close();
       //         action.proceed();});
       //     dialog.setCloseOnEsc(true);
       //     dialog.setCloseOnOutsideClick(true);
       //     dialog.add(new Label("Просто хотел сказать \"привет!\" перед тем как ты покинешь эту страницу."));
       //     //dialog.open(); // todo: удалить эту демку
       // }
    }
}