package ru.ulmc.investor.ui.view.monitoring;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ulmc.investor.data.entity.MonitoringEvent;
import ru.ulmc.investor.service.MonitoringService;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.util.PageParams;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.view.CommonPage;
import ru.ulmc.investor.user.Permission;

@SpringComponent
@UIScope
@TopLevelPage(menuName = "Мониторинг", order = 2)
@Route(value =  "monitor", layout = MainLayout.class)
public class MonitoringPage extends CommonPage {
    public static final PageParams PAGE = PageParams
            .from(Permission.MONITORING_READ, Permission.MONITORING_WRITE).build();
    private final UserService userService;
    private final MonitoringService monitoringService;
    private Grid<MonitoringEvent> grid;

    @Autowired
    public MonitoringPage(UserService userService, MonitoringService monitoringService) {
        super(userService, PAGE);
        this.userService = userService;
        this.monitoringService = monitoringService;
        initPage();
    }

    private void initPage() {
        grid = new Grid<>();
        grid.setSizeFull();
        grid.addColumn(MonitoringEvent::getId).setHeader("Id").setResizable(true).setFlexGrow(1);
        grid.addColumn(MonitoringEvent::getUser).setHeader("User").setWidth("120px");
        grid.addColumn(MonitoringEvent::getName).setHeader("Name").setWidth("120px");
        grid.addColumn(MonitoringEvent::getTimeAndDate).setHeader("Date and Time");
        grid.addColumn(MonitoringEvent::getDescription).setHeader("Description").setFlexGrow(10);
        layout.add(grid);
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу
        grid.setItems();
        grid.setItems(monitoringService.getEvents());
    }

    @Override
    public void onExit(BeforeLeaveEvent beforeLeaveEvent) {
        //Эта страница для виду...
    }
}
