package ru.ulmc.investor.ui.view.information;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.GeneratedVaadinDialog;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.ulmc.investor.data.entity.LastPrice;
import ru.ulmc.investor.event.dto.PriceUpdateEvent;
import ru.ulmc.investor.event.listeners.Registration;
import ru.ulmc.investor.event.listeners.StaticUpdateBroadcaster;
import ru.ulmc.investor.service.MarketService;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.CommonLightModel;
import ru.ulmc.investor.ui.entity.PortfolioLightModel;
import ru.ulmc.investor.ui.entity.position.PositionViewModel;
import ru.ulmc.investor.ui.util.Notify;
import ru.ulmc.investor.ui.util.PageParams;
import ru.ulmc.investor.ui.util.PositionStatusFilter;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.view.CommonPage;
import ru.ulmc.investor.ui.view.information.editor.ClosedPositionEditor;
import ru.ulmc.investor.ui.view.information.editor.FullPositionEditor;
import ru.ulmc.investor.ui.view.information.editor.OpenPositionEditor;
import ru.ulmc.investor.user.Permission;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static ru.ulmc.investor.ui.util.RouterUtil.navigateTo;
import static ru.ulmc.investor.ui.util.RouterUtil.unescapeParams;

@Slf4j
@PageTitle("Позиции")
@HtmlImport("frontend://src/position/position-name-cell.html")
@HtmlImport("frontend://src/position/position-date.html")
@HtmlImport("frontend://src/position/position-profit.html")
@HtmlImport("frontend://src/position/price-change.html")
@TopLevelPage(menuName = "Позиции", order = 3)
@Route(value = "positions", layout = MainLayout.class)
public class PositionsPage extends CommonPage implements HasUrlParameter<String> {
    public static final PageParams PAGE = PageParams.from(Permission.INFORMATION_READ).build();

    private final UI currentUi;
    private final ClosedPositionEditor closedPositionEditor;
    private final FullPositionEditor fullPositionEditor;
    private final MarketService quoteService;
    private final StaticUpdateBroadcaster staticBroadcaster;
    private final Map<String, Collection<PositionViewModel>> perSymbolPositions = new ConcurrentHashMap<>();
    private final AtomicBoolean enableAutoUpdate = new AtomicBoolean();
    private final Registration registration;
    private StocksService stocksService;
    private OpenPositionEditor openPositionEditor;
    private TreeGrid<PositionViewModel> grid;
    private HorizontalLayout controlsLayout;
    private ComboBox<PortfolioLightModel> portfolioComboBox;
    private ComboBox<PositionStatusFilter> statusFilter;
    private Checkbox enableRefreshCheckbox;
    private PositionSumComponent sumResultComponent;
    private Button addBtn;
    private List<PortfolioLightModel> allPortfoliosInfo;

    @Autowired
    public PositionsPage(UserService userService,
                         StocksService stocksService,
                         FullPositionEditor fullPositionEditor,
                         OpenPositionEditor openPositionEditor,
                         ClosedPositionEditor closedPositionEditor,
                         MarketService quoteService,
                         StaticUpdateBroadcaster staticBroadcaster) {
        super(userService, PAGE);
        this.stocksService = stocksService;
        this.fullPositionEditor = fullPositionEditor;
        this.openPositionEditor = openPositionEditor;
        this.closedPositionEditor = closedPositionEditor;
        this.quoteService = quoteService;
        this.staticBroadcaster = staticBroadcaster;
        this.openPositionEditor.addOpenedChangeListener(this::onEditorStateChange);
        this.fullPositionEditor.addOpenedChangeListener(this::onEditorStateChange);
        this.closedPositionEditor.addOpenedChangeListener(this::onEditorStateChange);
        this.currentUi = UI.getCurrent();
        init();
        registration = staticBroadcaster.subscribe(this::onUpdateEvent);
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        if (portfolioComboBox.getValue() == null && statusFilter.getValue() == null) {
            defaultSelect();
            long id = Optional.ofNullable(portfolioComboBox.getValue())
                    .map(CommonLightModel::getId)
                    .orElse(-1L);
            applyFilters(id);
        }
    }

    @Override
    public void onExit(BeforeLeaveEvent beforeLeaveEvent) {
        //todo: handle leave event
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        registration.unregister();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @WildcardParameter String s) {
        if (s != null) {
            log.debug("With Parameter {}", s);
            List<String> params = unescapeParams(s);
            if (params.size() == 2) {
                selectPortfolioFromParam(params).ifPresent(portfolioComboBox::setValue);
                statusFilter.setValue(PositionStatusFilter.valueOf(params.get(1)));
                applyFilters(Long.valueOf(params.get(0)));
            } else {
                // defaultSelect();
            }
        }
    }

    void onUpdateEvent(PriceUpdateEvent event) {
        final Collection<LastPrice> lastPrices = event.getLastPrices();
        if (currentUi == null) {
            registration.unregister();
            return;
        }
        try {
            currentUi.access(() -> {
                lastPrices.forEach(this::updateLastPrice);
                Notify.fastToast("Price update event received"); //debug purpose
            });
        } catch (UIDetachedException ex) {
            registration.unregister();
        }
    }

    private void onEditorStateChange(GeneratedVaadinDialog.OpenedChangeEvent<Dialog> event) {
        if (!event.isOpened()) {
            onFilterStateChange(null);
        }
    }

    private void findAndUpdateLastPrice() {
        partitionPositionsMap().forEach(this::packedUpdateRequest);
    }

    private List<Map<String, Collection<PositionViewModel>>> partitionPositionsMap() {
        List<Map<String, Collection<PositionViewModel>>> listOfMaps = new ArrayList<>();
        int counter = 5;
        Map<String, Collection<PositionViewModel>> map = new HashMap<>();
        for (val entry : perSymbolPositions.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
            if (--counter <= 0) {
                map = new HashMap<>();
                listOfMaps.add(map);
            }
        }
        if (counter != 0) {
            listOfMaps.add(map);
        }
        return listOfMaps;
    }

    private void packedUpdateRequest(Map<String, Collection<PositionViewModel>> pack) {
        currentUi.access(() -> doMarketDataUpdate(pack));
    }

    private void doMarketDataUpdate(Map<String, Collection<PositionViewModel>> pack) {
        quoteService.getBatchLastPrices(pack.keySet())
                .forEach(this::updateLastPrice);
        quoteService.getKeyStats(pack);
        pack.values().stream()
                .flatMap(Collection::stream)
                .forEach(i -> grid.getDataProvider().refreshItem(i));

    }

    private void updateLastPrice(LastPrice lastPrice) {
        val models = perSymbolPositions.getOrDefault(lastPrice.getSymbol(), emptyList());
        models.forEach(model -> {
            model.setMarketPrice(lastPrice.getLastPrice());
            //PriceChange priceChange = model.getPriceChange();
            //if (priceChange.isPresent()) {
            //    priceChange.setDay(PriceChange.Value.from(lastPrice.));
            //}
            grid.getDataProvider().refreshItem(model);
        });
    }

    private void onFilterStateChange(AbstractField.ComponentValueChangeEvent changeEvent) {
        log.trace("onFilterStateChange {}", changeEvent);

        PortfolioLightModel value = portfolioComboBox.getValue();
        boolean defined = value != null;
        statusFilter.setEnabled(defined);
        addBtn.setEnabled(defined);

        if (defined) {
            PositionStatusFilter status = statusFilter.getValue();
            String name = null;
            if (status != null) {
                name = status.name();
            }
            if (changeEvent == null || changeEvent.isFromClient()) {
                navigateTo(this.getClass(), String.valueOf(value.getId()), name);
            }
        }
    }

    private void init() {
        initAutoUpdateCheckbox();
        initGrid();
        initControlLayout();
        initMainLayout();
        loadPortfolioData();
    }

    private void applyFilters(long portfolioId) {
        List<PositionViewModel> positions = applyFilterByStatus(portfolioId);
        perSymbolPositions.clear();
        positions.forEach(this::addToPositionsMap);
        grid.setItems(getRootItems(), this::getChildrenForTreeGrid);

        sumResultComponent.update(positions);
        findAndUpdateLastPrice();
    }

    private Collection<PositionViewModel> getChildrenForTreeGrid(PositionViewModel p) {
        if (!p.isParent()) {
            return emptyList();
        }
        return perSymbolPositions.get(p.getSymbol().getCode());
    }

    private boolean addToPositionsMap(PositionViewModel pos) {
        return perSymbolPositions.computeIfAbsent(pos.getStockCode(), s -> new HashSet<>()).add(pos);
    }

    private List<PositionViewModel> getRootItems() {
        return perSymbolPositions.values().stream()
                .map(PositionViewModel::makeParentFrom)
                .collect(toList());
    }

    private void initGrid() {
        grid = new TreeGrid<>();
        grid.addHierarchyColumn(vm -> "")
                .setFlexGrow(0)
                .setHeader("");
        grid.setSizeFull();

        val nameCol = grid.addColumn(getNameRenderer())
                .setFlexGrow(10)
                .setHeader("Название");
        grid.addColumn(getDateRenderer())
                .setFlexGrow(5)
                .setHeader("Дата");
        grid.addColumn(PositionViewModel::getQuantity)
                .setFlexGrow(5)
                .setHeader("Размер");
        grid.addColumn(getChangeRenderer())
                .setFlexGrow(5)
                .setHeader("Изменения");
        grid.addColumn(getProfitRenderer())
                .setFlexGrow(5)
                .setHeader("Цена");
        val sumCol = grid.addColumn(getSumRenderer())
                .setFlexGrow(5)
                .setHeader("Сумма");
        grid.addComponentColumn(this::createRowControls)
                .setFlexGrow(0)
                .setWidth("150px")
                .setHeader("Действия");
        sumResultComponent = new PositionSumComponent();
        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(nameCol).setComponent(enableRefreshCheckbox);
        footerRow.getCell(sumCol).setComponent(sumResultComponent);
    }

    private void initControlLayout() {
        initStatusFilter();
        initPortfolioFilter();
        initAddButton();
        initControlsLayout();
    }

    private void initAutoUpdateCheckbox() {
        enableRefreshCheckbox = new Checkbox("Автообновление рынка");
        enableRefreshCheckbox.addValueChangeListener(event -> enableAutoUpdate.set(event.getValue()));
        enableRefreshCheckbox.setValue(true);
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

    private Renderer<PositionViewModel> getProfitRenderer() {
        return TemplateRenderer.<PositionViewModel>of("<position-profit position='[[item.prices]]'></position-profit>")
                .withProperty("prices", PositionViewModel::getPrices);
    }

    private Renderer<PositionViewModel> getChangeRenderer() {
        return TemplateRenderer.<PositionViewModel>of("<price-change price='[[item.prices]]'></price-change>")
                .withProperty("prices", PositionViewModel::getPrices);
    }

    private Renderer<PositionViewModel> getSumRenderer() {
        return TemplateRenderer.<PositionViewModel>of("<position-profit position='[[item.total]]'></position-profit>")
                .withProperty("total", PositionViewModel::getTotals);
    }

    private void defaultSelect() {
        allPortfoliosInfo.stream().findFirst().ifPresent(portfolioComboBox::setValue);
        statusFilter.setValue(PositionStatusFilter.OPEN);
    }

    private void initStatusFilter() {
        statusFilter = new ComboBox<>("Статус");
        statusFilter.setAllowCustomValue(false);
        statusFilter.setPreventInvalidInput(true);
        statusFilter.setEnabled(false);
        statusFilter.setItemLabelGenerator(PositionStatusFilter::getDescription);
        statusFilter.setItems(PositionStatusFilter.values());
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
        addBtn.addClickListener(event -> openPositionEditor.create(portfolioComboBox.getValue()));
    }

    private void initControlsLayout() {
        controlsLayout = new HorizontalLayout();
        controlsLayout.setWidth("100%");
        controlsLayout.add(portfolioComboBox, statusFilter, addBtn);
        controlsLayout.setAlignItems(FlexComponent.Alignment.END);
    }

    private void loadPortfolioData() {
        allPortfoliosInfo = stocksService.getAllPortfoliosInfo();
        portfolioComboBox.setItems(allPortfoliosInfo);
    }

    private Component createRowControls(PositionViewModel pvm) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        if (!pvm.isClosed()) {
            Button closePositionBtn = new Button(new Icon(VaadinIcon.CLOSE_BIG));
            closePositionBtn.getElement().setAttribute("title", "Закрыть позицию");
            closePositionBtn.addClickListener(buttonClickEvent -> closedPositionEditor.edit(pvm));
            hl.add(closePositionBtn);
        }
        if (!pvm.isParent()) {
            Button editBtn = new Button(new Icon(VaadinIcon.PENCIL));
            editBtn.addClickListener(buttonClickEvent -> {
                if (pvm.isClosed()) {
                    fullPositionEditor.edit(pvm);
                } else {
                    openPositionEditor.edit(pvm);
                }
            });
            hl.add(editBtn);
        }
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return hl;
    }

    private Optional<PortfolioLightModel> selectPortfolioFromParam(List<String> params) {
        long paramId = Long.parseLong(params.get(0));
        return allPortfoliosInfo.stream()
                .filter(s -> s.getId() == paramId)
                .findFirst();
    }

}
