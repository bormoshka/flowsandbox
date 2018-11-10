package ru.ulmc.investor.ui.view.information;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.PortfolioLightModel;
import ru.ulmc.investor.ui.entity.PositionViewModel;
import ru.ulmc.investor.ui.util.PageParams;
import ru.ulmc.investor.ui.util.PositionStatusFilter;
import ru.ulmc.investor.ui.util.RouterUtil;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.view.CommonPage;
import ru.ulmc.investor.ui.view.information.editor.ClosedPositionEditor;
import ru.ulmc.investor.ui.view.information.editor.FullPositionEditor;
import ru.ulmc.investor.ui.view.information.editor.OpenPositionEditor;
import ru.ulmc.investor.user.Permission;

import java.util.List;

import static java.util.Collections.emptyList;
import static ru.ulmc.investor.ui.util.RouterUtil.unescapeParams;

@Slf4j
@SpringComponent
@UIScope
@HtmlImport("frontend://src/position/position-name-cell.html")
@HtmlImport("frontend://src/position/position-date.html")
@HtmlImport("frontend://src/position/position-profit.html")
@TopLevelPage(menuName = "Позиции", order = 3)
@Route(value = "positions", layout = MainLayout.class)
public class PositionsPage extends CommonPage implements HasUrlParameter<String> {
    public static final PageParams PAGE = PageParams.from(Permission.INFORMATION_READ).build();

    private final ClosedPositionEditor closedPositionEditor;
    private final FullPositionEditor fullPositionEditor;
    private StocksService stocksService;
    private OpenPositionEditor openPositionEditor;
    private Grid<PositionViewModel> grid;
    private HorizontalLayout controlsLayout;
    private Button addBtn;
    private ComboBox<PortfolioLightModel> portfolioComboBox;
    private ComboBox<PositionStatusFilter> statusFilter;

    @Autowired
    public PositionsPage(UserService userService,
                         StocksService stocksService,
                         FullPositionEditor fullPositionEditor,
                         OpenPositionEditor openPositionEditor,
                         ClosedPositionEditor closedPositionEditor) {
        super(userService, PAGE);
        this.stocksService = stocksService;
        this.fullPositionEditor = fullPositionEditor;
        this.openPositionEditor = openPositionEditor;
        this.closedPositionEditor = closedPositionEditor;
        openPositionEditor.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                onFilterStateChange(event);
            }
        });
        init();
    }

    private void onFilterStateChange(Object someEvent) {
        PortfolioLightModel value = portfolioComboBox.getValue();
        boolean defined = value != null;
        statusFilter.setEnabled(defined);
        addBtn.setEnabled(defined);

        if (defined) {
            RouterUtil.navigateTo(this.getClass(), String.valueOf(value.getId()), statusFilter.getValue().name());
        }
    }

    private void init() {
        initGrid();
        initControlLayout();
        initMainLayout();
    }

    private void applyFilters(long portfolioId) {
        List<PositionViewModel> positions = applyFilterByStatus(portfolioId);
        grid.setItems(positions);
    }

    private void initGrid() {
        grid = new Grid<>();
        grid.setSizeFull();
        //todo: оформить красиво. не зря же на flow
        grid.addColumn(getNameRenderer())
                .setFlexGrow(10)
                .setHeader("Название");
        grid.addColumn(getDateRenderer())
                .setFlexGrow(5)
                .setHeader("Дата");
        grid.addColumn(PositionViewModel::getQuantity)
                .setFlexGrow(5)
                .setHeader("Размер");
        grid.addColumn(getPriceRenderer())
                .setFlexGrow(5)
                .setHeader("Цена");
        grid.addComponentColumn(this::createRowControls)
                .setFlexGrow(0)
                .setWidth("150px")
                .setHeader("Действия");
    }

    private void initControlLayout() {
        initStatusFilter();
        initPortfolioFilter();
        initAddButton();
        initControlsLayout();
        loadPortfolioData();
    }

    private void initMainLayout() {
        layout.add(controlsLayout);
        layout.add(grid);
    }

    private List<PositionViewModel> applyFilterByStatus(long portfolioId) {
        PositionStatusFilter value = statusFilter.getValue();
        if (value != null) {
            switch (value) {
                case OPEN:
                    return stocksService.getAllOpenPositions(portfolioId);
                case CLOSED:
                    return stocksService.getClosedPositions(portfolioId);
                case ALL:
                    return stocksService.getAllPositions(portfolioId);
            }
        }
        return emptyList();
    }

    private Renderer<PositionViewModel> getNameRenderer() {
        return TemplateRenderer.<PositionViewModel>of("<position-info position='[[item.position]]'></position-info>")
                .withProperty("position", p -> p);
    }

    private Renderer<PositionViewModel> getDateRenderer() {
        return TemplateRenderer.<PositionViewModel>of("<position-date position='[[item.position]]'></position-date>")
                .withProperty("position", p -> p);
    }

    private Renderer<PositionViewModel> getPriceRenderer() {
        return TemplateRenderer.<PositionViewModel>of("<position-profit position='[[item.position]]'></position-profit>")
                .withProperty("position", p -> p);
    }

    private void initStatusFilter() {
        statusFilter = new ComboBox<>("Статус");
        statusFilter.setAllowCustomValue(false);
        statusFilter.setPreventInvalidInput(true);
        statusFilter.setEnabled(false);
        statusFilter.setItemLabelGenerator(PositionStatusFilter::getDescription);
        statusFilter.setItems(PositionStatusFilter.values());
        statusFilter.setValue(PositionStatusFilter.OPEN);
        statusFilter.addValueChangeListener(this::onFilterStateChange);
    }

    private void initPortfolioFilter() {
        portfolioComboBox = new ComboBox<>("Портфолио");
        portfolioComboBox.setWidth("300px");
        portfolioComboBox.setAllowCustomValue(false);
        portfolioComboBox.setItemLabelGenerator(PortfolioLightModel::getName);
        portfolioComboBox.setAutofocus(true);
        portfolioComboBox.addValueChangeListener(this::onFilterStateChange);
        portfolioComboBox.setRequired(true);
    }

    private void initAddButton() {
        addBtn = new Button("Добавить", new Icon(VaadinIcon.PLUS));
        addBtn.setEnabled(false);
        addBtn.addClickListener(buttonClickEvent -> {
            openPositionEditor.create(portfolioComboBox.getValue());
        });
    }

    private void initControlsLayout() {
        controlsLayout = new HorizontalLayout();
        controlsLayout.setWidth("100%");
        controlsLayout.add(portfolioComboBox, statusFilter, addBtn);
        controlsLayout.setAlignItems(FlexComponent.Alignment.END);
    }

    private void loadPortfolioData() {
        List<PortfolioLightModel> allPortfoliosInfo = stocksService.getAllPortfoliosInfo();
        portfolioComboBox.setItems(allPortfoliosInfo);
        if (!allPortfoliosInfo.isEmpty()) {
            portfolioComboBox.setValue(allPortfoliosInfo.get(0));
        }
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу данными

    }

    private Component createRowControls(PositionViewModel pvm) {
        Button editBtn = new Button(new Icon(VaadinIcon.PENCIL));
        editBtn.addClickListener(buttonClickEvent -> {
            if (pvm.isClosed()) {
                fullPositionEditor.edit(pvm);
            } else {
                openPositionEditor.edit(pvm);
            }
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        if (!pvm.isClosed()) {
            Button closePositionBtn = new Button(new Icon(VaadinIcon.CLOSE_BIG));
            closePositionBtn.getElement().setAttribute("title", "Закрыть позицию");
            closePositionBtn.addClickListener(buttonClickEvent -> closedPositionEditor.edit(pvm));
            hl.add(closePositionBtn);
        }
        hl.add(editBtn);
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return hl;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @WildcardParameter String s) {
        if (s != null) {
            log.debug("With Parameter {}", s);
            List<String> params = unescapeParams(s);
            if (params.size() == 2) {
                statusFilter.setValue(PositionStatusFilter.valueOf(params.get(1)));
                applyFilters(Long.valueOf(params.get(0)));
            }
        }
    }
}
