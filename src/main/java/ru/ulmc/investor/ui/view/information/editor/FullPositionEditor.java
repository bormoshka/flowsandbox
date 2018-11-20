package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.NonNull;
import ru.ulmc.investor.data.entity.Symbol;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.SymbolViewModel;
import ru.ulmc.investor.ui.entity.PortfolioLightModel;
import ru.ulmc.investor.ui.entity.PositionViewModel;

import java.util.List;
import java.util.Locale;

import static ru.ulmc.investor.ui.util.Format.DECIMAL_PATTERN;
import static ru.ulmc.investor.ui.util.Format.LOCALE;
import static ru.ulmc.investor.ui.util.UiUtils.getCalendarI18n;

@UIScope
@SpringComponent
@Route(value = "positions/edit", layout = MainLayout.class)
public class FullPositionEditor extends CommonPopupEditor<PositionViewModel> {
    TextField quantity = new TextField("Количество");
    TextField currencyPrice = new TextField("Цена валюты (откр.)");
    TextField price = new TextField("Цена открытия");
    DatePicker date = new DatePicker("Дата открытия");
    TextField time = new TextField("Время открытия");
    TextField priceClosed = new TextField("Цена закрытия");
    TextField currencyPriceClosed = new TextField("Цена валюты (закр.)");
    DatePicker dateClosed = new DatePicker("Дата закрытия");
    TextField timeClosed = new TextField("Время открытия");
    ComboBox<SymbolViewModel> stockComboBox = new ComboBox<>("Позиция");
    private StocksService stocksService;

    public FullPositionEditor(StocksService stocksService) {
        this.stocksService = stocksService;
        setWidth("650px");
    }

    @Override
    protected void layout() {
        VerticalLayout verticalLayout = new VerticalLayout(getTitle("Редактирование позиции:"));
        verticalLayout.setSizeFull();
        verticalLayout.add(getCommonComponents());
        boolean showAll = showOpenComponents() && showClosedComponents();

        if (showAll) {
            verticalLayout.add(new H4("Характеристики открытия:"));
        }

        if (showAll || showOpenComponents()) {
            Div group = getComponentsGroup(price, currencyPrice, date, time);
            verticalLayout.add(group);
        }

        if (showAll) {
            verticalLayout.add(new H4("Характеристики закрытия:"));
        }

        if (showAll || showClosedComponents()) {
            Div group = getComponentsGroup(priceClosed, currencyPriceClosed, dateClosed, timeClosed);
            verticalLayout.add(group);
        }
        verticalLayout.add(getControls());
        add(verticalLayout);
    }

    @Override
    protected void initBinder() {
        binder = new PositionBinder(this);
    }

    @Override
    protected void initFields() {
        quantity.setPattern("[0-9]*");
        quantity.setPreventInvalidInput(true);
        quantity.setWidth("50%");
        quantity.setRequired(true);

        if (showOpenComponents()) {
            initFields(date, time, price, currencyPrice);
        }
        if (showClosedComponents()) {
            initFields(dateClosed, timeClosed, priceClosed, currencyPriceClosed);
        }

        initFirstSelectionComponent();
    }

    @Override
    protected void onSave(PositionViewModel bean) {
        stocksService.save(PositionViewModel.toEntity(bean));
        String text = "Позиция \"" + bean.getSymbol().getName() + "\" успешно сохранена!";
        Notification.show(text, 2500, Notification.Position.MIDDLE);
    }


    private void initFields(DatePicker date, TextField time, TextField price, TextField currencyPrice) {
        date.setRequired(true);
        date.setLocale(LOCALE);
        date.setI18n(getCalendarI18n());
        date.setWidth("50%");

        time.setPattern("^(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$");
        time.setPlaceholder("Не важно"); //todo: что-нибудь более подходящее придумать
        time.setWidth("50%");

        price.setPattern(DECIMAL_PATTERN);
        price.setRequired(true);
        price.setWidth("50%");

        currencyPrice.setPattern(DECIMAL_PATTERN);
        currencyPrice.setRequired(true);
        currencyPrice.setValue("1");
        currencyPrice.setWidth("50%");
    }

    boolean showOpenComponents() {
        return true;
    }

    boolean showClosedComponents() {
        return true;
    }

    private HorizontalLayout getCommonComponents() {
        HorizontalLayout nameLine = new HorizontalLayout(getFirstSelectionComponent(), quantity);
        nameLine.setWidth("100%");
        return nameLine;
    }

    private Div getComponentsGroup(TextField price, TextField currencyPrice, DatePicker date, TextField time) {
        HorizontalLayout priceLine = new HorizontalLayout(price, currencyPrice);
        priceLine.setWidth("100%");
        HorizontalLayout timeLine = new HorizontalLayout(date, time);
        timeLine.setWidth("100%");
        Div div = new Div(priceLine, timeLine);
        div.setWidth("100%");
        return div;
    }

    Component getFirstSelectionComponent() {
        return stockComboBox;
    }

    void initFirstSelectionComponent() {
        stockComboBox.setWidth("50%");
        stockComboBox.setAutofocus(true);
        stockComboBox.setAllowCustomValue(false);
        stockComboBox.setItemLabelGenerator(SymbolViewModel::getName);
        stockComboBox.setRenderer(TemplateRenderer.<SymbolViewModel>of(getTemplate())
                .withProperty("name", SymbolViewModel::getName)
                .withProperty("currency", SymbolViewModel::getCurrency)
                .withProperty("currencyClose", SymbolViewModel::getCloseCurrency)
                .withProperty("broker", stockViewModel -> stockViewModel.getBroker().getName())
                .withProperty("symbol", SymbolViewModel::getCode));
    }

    private String getTemplate() {
        return "<div class='position-combo'>[[item.name]]<br>" +
                "<span class='sub-text'>[[[item.symbol]]] | [[item.currency]]/[[item.currencyClose]] " +
                "| <span class='broker'>[[item.broker]]</span>" +
                "</span></div>";
    }

    public void create(@NonNull PortfolioLightModel parent) {
        edit(PositionViewModel.builder()
                .portfolio(parent)
                .symbol(SymbolViewModel.of(Symbol.empty()))
                .build());
    }

    public void edit(@NonNull PositionViewModel viewModel) {
        init();
        loadStockData();
        onEdit(viewModel);
    }

    private void loadStockData() {
        List<SymbolViewModel> stockPositions = stocksService.getStockPositions();
       // stockComboBox.setFilteredItems(stockPositions);
        stockComboBox.setItems(stockPositions);
    }

}
