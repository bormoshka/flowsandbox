package ru.ulmc.investor.ui.view.information;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.GeneratedVaadinDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.ulmc.investor.data.entity.Broker;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.service.UserService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.component.ConfirmDialog;
import ru.ulmc.investor.ui.entity.BrokerLightModel;
import ru.ulmc.investor.ui.entity.SymbolViewModel;
import ru.ulmc.investor.ui.util.Notify;
import ru.ulmc.investor.ui.util.PageParams;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.view.CommonPage;
import ru.ulmc.investor.ui.view.information.editor.BrokerEditor;
import ru.ulmc.investor.ui.view.information.editor.SymbolEditor;
import ru.ulmc.investor.user.Permission;

import static ru.ulmc.investor.ui.util.GridUtils.getRowControls;

@Slf4j
@PageTitle("Справочник")
@TopLevelPage(menuName = "Справочник", order = 3)
@Route(value = "stocks", layout = MainLayout.class)
public class InformationPage extends CommonPage {
    public static final PageParams PAGE = PageParams.from(Permission.INFORMATION_READ).build();
    private final StocksService stocksService;
    private final BrokerEditor brokerEditor;
    private final SymbolEditor symbolEditor;
    private final Grid<SymbolViewModel> stocksGrid = new Grid<>();
    private final Grid<BrokerLightModel> brokersGrid = new Grid<>();
    private final Button addBrokerBtn = new Button("Добавить брокера", new Icon(VaadinIcon.PLUS));
    private final Button addStockBtn = new Button("Добавить позицию", new Icon(VaadinIcon.PLUS));
    private final String uuid = UUID.randomUUID().toString();

    @Autowired
    public InformationPage(UserService userService,
                           StocksService stocksService,
                           BrokerEditor brokerEditor,
                           SymbolEditor symbolEditor) {
        super(userService, PAGE);
        this.stocksService = stocksService;
        this.brokerEditor = brokerEditor;
        this.symbolEditor = symbolEditor;

        brokerEditor.addOpenedChangeListener(this::onEditorClose);
        symbolEditor.addOpenedChangeListener(this::onEditorClose);
        init();
    }

    private void onEditorClose(GeneratedVaadinDialog.OpenedChangeEvent<Dialog> dialogCloseActionEvent) {
        if (!dialogCloseActionEvent.isOpened()) {
            reloadDataWithReselection();
        }
    }

    private void init() {
        initBrokersGrid();
        initStocksGrid();
        layout.add(initControls());
        VerticalLayout vl = new VerticalLayout(brokersGrid);
        vl.setWidth("500px");
        vl.setPadding(false);
        HorizontalLayout horizontalLayout = new HorizontalLayout(vl, stocksGrid);
        horizontalLayout.setFlexGrow(1, vl);
        horizontalLayout.setFlexGrow(100, stocksGrid);
        horizontalLayout.setSizeFull();
        layout.add(horizontalLayout);
    }

    @Override
    public void onEnter(BeforeEnterEvent beforeEnterEvent) {
        //вот тут начинаем заполнять страницу
        log.debug("VIEW UUID: {}", uuid);
        reloadDataWithReselection();
    }

    @Override
    public void onExit(BeforeLeaveEvent beforeLeaveEvent) {
        // brokersGrid.deselectAll();
        // stocksGrid.deselectAll();
    }

    private void reloadDataWithReselection() {
        val firstSelectedItem = brokersGrid.getSelectionModel().getFirstSelectedItem();
        reloadBrokers();
        firstSelectedItem.ifPresent(broker -> {
            brokersGrid.select(broker);
            reloadStocks(broker);
        });
    }

    private void reloadBrokers() {
        List<BrokerLightModel> brokers = stocksService.getBrokers();
        brokersGrid.setItems(brokers);
    }

    private void initStocksGrid() {
        stocksGrid.addColumn(SymbolViewModel::getName)
                .setHeader("Название")
                .setFlexGrow(4);
        stocksGrid.addColumn(SymbolViewModel::getCode)
                .setHeader("Код");
        stocksGrid.addColumn(model -> model.getCurrency() + " -> " + model.getCloseCurrency())
                .setHeader("Валюты");
        stocksGrid.addComponentColumn(this::createRowControls)
                .setWidth("130px");
        stocksGrid.setSizeFull();
    }

    private void initBrokersGrid() {
        brokersGrid.setSizeFull();
        brokersGrid.addColumn(BrokerLightModel::getName)
                .setHeader("Название")
                .setFlexGrow(4);
        brokersGrid.addComponentColumn(this::createRowControls)
                .setWidth("130px");

        brokersGrid.addSelectionListener(event -> {
            addStockBtn.setEnabled(false);
            event.getFirstSelectedItem().ifPresent(broker -> {
                reloadStocks(broker);
                addStockBtn.setEnabled(true);
            });
        });
    }

    private void reloadStocks(BrokerLightModel broker) {
        List<SymbolViewModel> stocks = stocksService.getStockPositionsByBrokerId(broker.getId());
        stocksGrid.setItems(stocks);
    }

    private Component initControls() {
        addBrokerBtn.setEnabled(true);
        addBrokerBtn.addClickListener(buttonClickEvent -> brokerEditor.create());

        addStockBtn.setEnabled(false);
        addStockBtn.addClickListener(buttonClickEvent ->
                brokersGrid.getSelectionModel().getFirstSelectedItem().ifPresent(symbolEditor::create));

        return new HorizontalLayout(addBrokerBtn, addStockBtn);
    }

    private Component createRowControls(BrokerLightModel model) {

        Button editBtn = new Button(new Icon(VaadinIcon.PENCIL));
        editBtn.addClickListener(buttonClickEvent -> {
            Optional<Broker> broker = stocksService.getBroker(model.getId());
            if (broker.isPresent()) {
                brokerEditor.edit(broker.get());
            } else {
                Notify.error("Ошибка! Запись для редактирования не найдена");
            }
        });
        Button removeBtn = new Button(new Icon(VaadinIcon.TRASH));
        removeBtn.addClickListener(buttonClickEvent -> {
            String text = "Эта операция безвозвратно удалит брокера и все его активы \""
                    + model.getName() + "\"?";
            ConfirmDialog.show(text, () -> {
                stocksService.removeBroker(model.getId());
                reloadDataWithReselection();
            });
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        hl.add(editBtn, removeBtn);
        return hl;
    }

    private Component createRowControls(SymbolViewModel model) {
        return getRowControls(e -> symbolEditor.edit(model),
                e -> ConfirmDialog.show(getRemoveConfirmText(model),
                () -> onRemoveConfirmed(model)));
    }

    private void onRemoveConfirmed(SymbolViewModel model) {
        stocksService.removeStock(model.getId());
        reloadDataWithReselection();
    }

    private String getRemoveConfirmText(SymbolViewModel model) {
        return "Эта операция безвозвратно удалит справочный актив \""
                + model.getName() + "\"?";
    }
}