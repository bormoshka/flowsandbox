package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.combobox.ComboBox;
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
import ru.ulmc.investor.data.entity.StockPosition;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.BrokerLightModel;
import ru.ulmc.investor.ui.entity.CommonLightModel;
import ru.ulmc.investor.ui.entity.StockViewModel;
import ru.ulmc.investor.ui.util.Notify;

import java.util.List;
import java.util.Locale;

@UIScope
@SpringComponent
@Route(value = "stocks/edit", layout = MainLayout.class)
public class StockEditor extends CommonPopupEditor<StockViewModel> {
    private static final Locale LOCALE = new Locale("ru", "RU");
    private static final String DECIMAL_PATTERN = "\\d+(\\.\\d{1,10})?";

    private TextField name = new TextField("Название");
    private TextField code = new TextField("Код");
    private ComboBox<Currency> positionCurrency = new ComboBox<>("Валюта позиции");
    private ComboBox<Currency> positionCloseCurrency = new ComboBox<>("Валюта закрытия позиции");
    private ComboBox<BrokerLightModel> broker = new ComboBox<>("Брокер");

    private StocksService stocksService;

    public StockEditor(StocksService stocksService) {
        this.stocksService = stocksService;
        init();
    }

    @Override
    protected void layout() {
        HorizontalLayout controls = initControls();

        HorizontalLayout descRow = new HorizontalLayout(name, code);
        HorizontalLayout currencyRow = new HorizontalLayout(positionCurrency, positionCloseCurrency);

        VerticalLayout verticalLayout = new VerticalLayout(descRow, currencyRow, broker, controls);
        add(verticalLayout);
    }

    @Override
    protected void initBinder() {
        binder = new BeanValidationBinder<>(StockViewModel.class);
        binder.bind(name, StockViewModel::getName, StockViewModel::setName);
        binder.bind(code, StockViewModel::getCode, StockViewModel::setCode);
        binder.bind(positionCurrency, StockViewModel::getCurrency, StockViewModel::setCurrency);
        binder.bind(positionCloseCurrency, StockViewModel::getCloseCurrency, StockViewModel::setCloseCurrency);
        binder.bind(broker, StockViewModel::getBroker, StockViewModel::setBroker);

        binder.setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        binder.addValueChangeListener(valueChangeEvent -> changed = true);
    }


    @Override
    protected void onSave(StockViewModel bean) {
        stocksService.save(StockViewModel.toEntity(bean));
        String text = "Позиция \"" + bean.getName() + "\" успешно сохранена!";
        Notify.info(text);
    }

    @Override
    protected void initFields() {

        positionCurrency.setItems(Currency.values());
        positionCurrency.setRequired(true);

        positionCloseCurrency.setItems(Currency.values());
        positionCloseCurrency.setPlaceholder("Идентична валюте открытия");
        positionCloseCurrency.setRequired(false);

        broker.setRequired(true);
        broker.setPreventInvalidInput(true);
        broker.setWidth("100%");
        broker.setItemLabelGenerator(CommonLightModel::getName);
    }

    private void loadBrokerComboBoxData() {
        List<BrokerLightModel> brokers = stocksService.getBrokers();

        broker.setItems(brokers);
        broker.setFilteredItems(brokers);
    }

    public void create(BrokerLightModel brokerLightModel) {
        StockViewModel model = StockViewModel.of(StockPosition.empty());
        model.setBroker(brokerLightModel);
        edit(model);
    }

    public void edit(@NonNull StockViewModel viewModel) {
        loadBrokerComboBoxData();
        onEdit(viewModel);
    }
}