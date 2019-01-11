package ru.ulmc.investor.ui.view.information;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.springframework.beans.factory.annotation.Autowired;

import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.component.ConfirmDialog;
import ru.ulmc.investor.ui.entity.PortfolioViewModel;
import ru.ulmc.investor.ui.util.PageParams;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.view.CommonPage;
import ru.ulmc.investor.ui.view.information.editor.PortfolioEditor;

import static ru.ulmc.investor.ui.util.GridUtils.getRowControls;
import static ru.ulmc.investor.user.Permission.INFORMATION_READ;

@PageTitle("Портфолио")
@TopLevelPage(menuName = "Портфолио", order = 2)
@Route(value = "portfolio", layout = MainLayout.class)
public class PortfolioPage extends CommonPage {
    private static final PageParams PAGE = PageParams.from(INFORMATION_READ).build();
    private StocksService stocksService;
    private PortfolioEditor portfolioEditor;
    private Grid<PortfolioViewModel> grid;
    private HorizontalLayout controlsLayout;
    private Button createBtn;

    @Autowired
    public PortfolioPage(UserService userService,
                         StocksService stocksService,
                         PortfolioEditor portfolioEditor) {
        super(userService, PAGE);
        this.stocksService = stocksService;
        this.portfolioEditor = portfolioEditor;
        portfolioEditor.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                reloadData();
            }
        });
        init();
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу данными
        reloadData();
    }

    @Override
    public void onExit(BeforeLeaveEvent beforeLeaveEvent) {
        //todo: handle leave event
    }

    private void reloadData() {
        grid.setItems(stocksService.getAllPortfolios());
    }

    private void init() {
        initControlLayout();
        initGrid();
        initMainLayout();
    }

    private void initMainLayout() {
        layout.add(controlsLayout);
        layout.add(grid);
    }

    private void initControlLayout() {
        createBtn = new Button("Добавить", new Icon(VaadinIcon.PLUS));
        createBtn.addClickListener(buttonClickEvent -> portfolioEditor.create());
        controlsLayout = new HorizontalLayout();
        controlsLayout.setWidth("100%");
        // Label pageLabel = new Label("Управление портфолио");
        //controlsLayout.add(pageLabel);
        controlsLayout.add(createBtn);
        controlsLayout.setAlignItems(FlexComponent.Alignment.END);
    }

    private void initGrid() {
        grid = new Grid<>();
        grid.setSizeFull();
        grid.addColumn(PortfolioViewModel::getName)
                .setFlexGrow(10)
                .setHeader("Название");
        grid.addColumn(PortfolioViewModel::getPositionsTotal)
                .setFlexGrow(5)
                .setHeader("Всего позиций");
        grid.addColumn(PortfolioViewModel::getTotalInvestedValue)
                .setFlexGrow(5)
                .setHeader("Всего инвестировано");
        grid.addComponentColumn(this::createRowControls)
                .setFlexGrow(0)
                .setWidth("150px");
        //.setHeader("Действия");
    }

    private Component createRowControls(PortfolioViewModel pvm) {
        return getRowControls(e -> portfolioEditor.edit(pvm),
                e -> ConfirmDialog.show(
                        getRemoveConfirmDialogText(pvm),
                        () -> onRemoveConfirmed(pvm)));
    }

    private String getRemoveConfirmDialogText(PortfolioViewModel pvm) {
        return "Удалить портфолио \"" + pvm.getName() + "\"?";
    }

    private void onRemoveConfirmed(PortfolioViewModel pvm) {
        stocksService.removePortfolio(pvm.getId());
        reloadData();
    }
}
