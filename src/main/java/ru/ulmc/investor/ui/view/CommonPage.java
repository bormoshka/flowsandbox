package ru.ulmc.investor.ui.view;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import lombok.Getter;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.util.PageParams;

public abstract class CommonPage extends Div implements HasElement, BeforeLeaveObserver,
        BeforeEnterObserver {
    protected UserService userService;
    @Getter
    private PageParams pageParams;
    protected VerticalLayout layout = new VerticalLayout();

    public CommonPage(UserService userService, PageParams pageParams) {
        this.userService = userService;
        this.pageParams = pageParams;
        setupLayout();
        addClassName("root-layout__content");
    }

    private void setupLayout() {
        layout.setSizeFull();
        setSizeFull();
        add(layout);
    }

    public abstract void onEnter(BeforeEnterEvent beforeEnterEvent);

    public abstract void onExit(BeforeLeaveEvent beforeLeaveEvent);

    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        onExit(beforeLeaveEvent);
    }

    @Override
    public final void beforeEnter(BeforeEnterEvent event) {
        if(pageParams.getReadPermission() != null
                && userService.getCurrentUserRole().hasPermission(pageParams.getReadPermission())) {
            onEnter(event);
        } else {
            event.rerouteTo("403");
        }
    }
}
