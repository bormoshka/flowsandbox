package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.NonNull;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.data.entity.Instrument;
import ru.ulmc.investor.data.entity.InstrumentType;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.BrokerLightModel;
import ru.ulmc.investor.ui.entity.CommonLightModel;
import ru.ulmc.investor.ui.entity.InstrumentViewModel;
import ru.ulmc.investor.ui.util.Notify;

import java.util.List;
import java.util.Locale;

@UIScope
@SpringComponent
@Route(value = "instrument/edit", layout = MainLayout.class)
public class InstrumentEditor extends CommonPopupEditor<InstrumentViewModel> {
    private static final Locale LOCALE = new Locale("ru", "RU");
    private static final String DECIMAL_PATTERN = "\\d+(\\.\\d{1,10})?";

    private TextField name = new TextField("Название");
    private TextField code = new TextField("Код");
    private ComboBox<Currency> positionCurrency = new ComboBox<>("Валюта позиции");
    private ComboBox<Currency> positionCloseCurrency = new ComboBox<>("Валюта закрытия позиции");
    private ComboBox<InstrumentType> instrumentType = new ComboBox<>("Тип инструмента");
    private ComboBox<BrokerLightModel> broker = new ComboBox<>("Брокер");

    private StocksService stocksService;

    public InstrumentEditor(StocksService stocksService) {
        this.stocksService = stocksService;
        init();
    }

    @Override
    protected void layout() {
        HorizontalLayout controls = getControls();

        HorizontalLayout descRow = new HorizontalLayout(name, code);
        descRow.setWidth("100%");
        HorizontalLayout currencyRow = new HorizontalLayout(instrumentType, positionCurrency, positionCloseCurrency);
        currencyRow.setWidth("100%");

        VerticalLayout verticalLayout = new VerticalLayout(getTitle("Редактирование инструмента:"),
                descRow, currencyRow, broker, controls);
        add(verticalLayout);
    }

    @Override
    protected void initBinder() {
        binder = new BeanValidationBinder<>(InstrumentViewModel.class);
        binder.bind(name, InstrumentViewModel::getName, InstrumentViewModel::setName);
        binder.bind(code, InstrumentViewModel::getCode, InstrumentViewModel::setCode);
        binder.bind(positionCurrency, InstrumentViewModel::getCurrency, InstrumentViewModel::setCurrency);
        binder.bind(positionCloseCurrency, InstrumentViewModel::getCloseCurrency, InstrumentViewModel::setCloseCurrency);
        binder.bind(broker, InstrumentViewModel::getBroker, InstrumentViewModel::setBroker);
        binder.bind(instrumentType, InstrumentViewModel::getType, InstrumentViewModel::setType);

        binder.setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        binder.addValueChangeListener(valueChangeEvent -> changed = true);
    }

    @Override
    protected void initFields() {
        name.setWidth("66.66%");

        instrumentType.setItems(InstrumentType.values());
        instrumentType.setFilteredItems(InstrumentType.values());
        instrumentType.setValue(InstrumentType.STOCK);
        instrumentType.setItemLabelGenerator(InstrumentType::getDescription);
        instrumentType.setRequired(true);
        instrumentType.setWidth("33.33%");

        positionCurrency.setItems(Currency.values());
        positionCurrency.setFilteredItems(Currency.values());
        positionCurrency.setRequired(true);
        positionCurrency.setWidth("33.33%");

        positionCloseCurrency.setItems(Currency.values());
        positionCloseCurrency.setFilteredItems(Currency.values());
        positionCloseCurrency.setPlaceholder("Идентична валюте открытия");
        positionCloseCurrency.setRequired(false);
        positionCloseCurrency.setWidth("33.33%");

        broker.setRequired(true);
        broker.setPreventInvalidInput(true);
        broker.setWidth("100%");
        broker.setItemLabelGenerator(CommonLightModel::getName);
    }

    @Override
    protected void onSave(InstrumentViewModel bean) {
        stocksService.save(InstrumentViewModel.toEntity(bean));
        String text = "Позиция \"" + bean.getName() + "\" успешно сохранена!";
        Notify.info(text);
    }

    private void loadBrokerComboBoxData() {
        List<BrokerLightModel> brokers = stocksService.getBrokers();

        broker.setItems(brokers);
        broker.setFilteredItems(brokers);
    }

    public void create(BrokerLightModel brokerLightModel) {
        InstrumentViewModel model = InstrumentViewModel.of(Instrument.empty());
        model.setBroker(brokerLightModel);
        edit(model);
    }

    public void edit(@NonNull InstrumentViewModel viewModel) {
        loadBrokerComboBoxData();
        onEdit(viewModel);
    }
}