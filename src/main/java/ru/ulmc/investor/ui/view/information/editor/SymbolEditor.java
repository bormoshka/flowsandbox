package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox.CustomValueSetEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.NonNull;
import ru.ulmc.investor.data.entity.*;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.service.MarketService;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.component.CompanyComponent;
import ru.ulmc.investor.ui.entity.BrokerLightModel;
import ru.ulmc.investor.ui.entity.CommonLightModel;
import ru.ulmc.investor.ui.entity.CompanyViewModel;
import ru.ulmc.investor.ui.entity.SymbolViewModel;
import ru.ulmc.investor.ui.util.Notify;

import java.util.*;

import static java.util.stream.Collectors.toList;

@UIScope
@SpringComponent
@Route(value = "symbol/edit", layout = MainLayout.class)
public class SymbolEditor extends CommonPopupEditor<SymbolViewModel> {
    private static final Locale LOCALE = new Locale("ru", "RU");
    private static final String DECIMAL_PATTERN = "\\d+(\\.\\d{1,10})?";
    private final MarketService marketService;
    private final StocksService stocksService;
    private ComboBox<CompanyViewModel> symbolCombo = new ComboBox<>("Код");
    private ComboBox<Currency> positionCurrency = new ComboBox<>("Валюта позиции");
    private ComboBox<Currency> positionCloseCurrency = new ComboBox<>("Валюта закрытия позиции");
    private ComboBox<SymbolType> instrumentType = new ComboBox<>("Тип инструмента");
    private ComboBox<StockExchange> stockExchange = new ComboBox<>("Биржа");
    private ComboBox<BrokerLightModel> broker = new ComboBox<>("Брокер");
    private CompanyComponent companyInfo = new CompanyComponent();
    private CompanyViewModel company;
    private HorizontalLayout currencyRow;

    public SymbolEditor(MarketService marketService, StocksService stocksService) {
        this.marketService = marketService;
        this.stocksService = stocksService;
        init();
        setWidth("600px");
    }

    @Override
    protected void layout() {
        HorizontalLayout controls = getControls();

        HorizontalLayout descRow = new HorizontalLayout(symbolCombo, stockExchange);
        descRow.setWidth("100%");
        HorizontalLayout companyRow = new HorizontalLayout(companyInfo);
        companyRow.setWidth("100%");
        currencyRow = new HorizontalLayout(instrumentType, positionCurrency, positionCloseCurrency);
        currencyRow.setWidth("100%");
        currencyRow.setVisible(false);

        VerticalLayout verticalLayout = new VerticalLayout(getTitle("Редактирование инструмента:"),
                descRow, companyRow, currencyRow, broker, controls);
        add(verticalLayout);
    }

    @Override
    protected void initBinder() {
        binder = new BeanValidationBinder<>(SymbolViewModel.class);
        //binder.bind(name, SymbolViewModel::getName, SymbolViewModel::setName);
        //binder.bind(symbolCombo, SymbolViewModel::getCode, SymbolViewModel::setCode);
        binder.bind(positionCurrency, SymbolViewModel::getCurrency, SymbolViewModel::setCurrency);
        binder.bind(positionCloseCurrency, SymbolViewModel::getCloseCurrency, SymbolViewModel::setCloseCurrency);
        binder.bind(broker, SymbolViewModel::getBroker, SymbolViewModel::setBroker);
        binder.bind(instrumentType, SymbolViewModel::getType, SymbolViewModel::setType);

        binder.setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        binder.addValueChangeListener(valueChangeEvent -> changed = true);
    }

    @Override
    protected void initFields() {
        //name.setWidth("66.66%");
        companyInfo.setVisible(false);

        initSymbolCombo();
        initStockExchangeCombo();
        initInstrumentTypeCombo();
        initCurrenciesCombos();
        initBrokerCombo();

        enableFields(false);
    }

    @Override
    protected void onSave(SymbolViewModel bean) {
        bean.setName(company.getName());
        bean.setCode(company.getSymbol());
        stocksService.save(SymbolViewModel.toEntity(bean));
        String text = "Позиция \"" + bean.getName() + "\" успешно сохранена!";
        Notify.info(text);
    }

    private void initSymbolCombo() {
        symbolCombo.setAllowCustomValue(true);
        symbolCombo.addValueChangeListener(event -> loadCompanyInfo(event.getValue()));
        symbolCombo.setEnabled(true);
        symbolCombo.setWidth("66.77%");
        symbolCombo.addCustomValueSetListener(this::onCustomSymbolComboBoxValue);
        symbolCombo.setItemLabelGenerator(CompanyViewModel::getSymbol);
    }

    private void onCustomSymbolComboBoxValue(CustomValueSetEvent<ComboBox<CompanyViewModel>> event) {
        if (!event.isFromClient()) {
            return;
        }
        String detail = event.getDetail();
        CompanyViewModel viewModel = CompanyViewModel.of(detail);
        Collection<CompanyViewModel> newCollection = getSymbolsComboData(detail);
        Optional<CompanyViewModel> first = newCollection.stream()
                .filter(companyViewModel -> companyViewModel.getSymbol()
                        .equalsIgnoreCase(viewModel.getSymbol()))
                .findFirst();
        if (first.isPresent()) {
            loadCompanyInfo(first.get());
        } else {
            symbolCombo.setErrorMessage("Инструмент с таким кодом не найден");
        }
    }

    private void initBrokerCombo() {
        broker.setRequired(true);
        broker.setPreventInvalidInput(true);
        broker.setWidth("100%");
        broker.setItemLabelGenerator(CommonLightModel::getName);
    }

    private void initCurrenciesCombos() {
        initCommonCurrencyComboBox(positionCurrency, true);
        initCommonCurrencyComboBox(positionCloseCurrency, false);
        positionCloseCurrency.setPlaceholder("Идентична валюте открытия");
    }

    private void initCommonCurrencyComboBox(ComboBox<Currency> combo, boolean required) {
        combo.setItems(Currency.values());
        combo.setFilteredItems(Currency.values());
        combo.setRequired(required);
        combo.setWidth("33.33%");
        combo.setItemLabelGenerator(this::getCurrencyLabel);
    }

    private String getCurrencyLabel(Currency item) {
        return item.name() + " (" + item.toString() + ")";
    }

    private void initInstrumentTypeCombo() {
        instrumentType.setItems(SymbolType.values());
        instrumentType.setFilteredItems(SymbolType.values());
        instrumentType.setValue(SymbolType.STOCK);
        instrumentType.setItemLabelGenerator(SymbolType::getDescription);
        instrumentType.setRequired(true);
        instrumentType.setWidth("33.33%");
    }

    private void initStockExchangeCombo() {
        stockExchange.setItems(StockExchange.values());
        stockExchange.setFilteredItems(StockExchange.values());
        stockExchange.setValue(StockExchange.UNKNOWN);
        stockExchange.setItemLabelGenerator(StockExchange::getName);
        stockExchange.setRequired(true);
        stockExchange.setWidth("33.33%");
    }

    private void enableFields(boolean enabled) {
        instrumentType.setEnabled(enabled);
        positionCurrency.setEnabled(enabled);
        positionCloseCurrency.setEnabled(enabled);
        stockExchange.setEnabled(enabled);
        saveBtn.setEnabled(enabled);
    }


    private void loadCompanyInfo(CompanyViewModel symbolCandidate) {
        if (symbolCandidate == null || symbolCandidate.getSymbol() == null) {
            onEmptyCompany(false);
            return;
        }
        if (symbolCandidate.isReliable()) {
            updateFieldsWithCompanyData(symbolCandidate);
            return;
        }
        Optional<CompanyInfo> response = marketService.getCompanyInfo(symbolCandidate.getSymbol());
        if (response.isPresent()) {
            updateFieldsWithCompanyData(CompanyViewModel.of(response.get()));
        } else {
            symbolCombo.setErrorMessage("Инструмент с таким кодом не найден");
        }
    }

    private void onEmptyCompany(boolean b) {
        enableFields(b);
        currencyRow.setVisible(b);
        companyInfo.setVisible(b);
    }

    private void updateFieldsWithCompanyData(CompanyViewModel company) {
        this.company = company;
        onEmptyCompany(true);
        companyInfo.update(company);
        instrumentType.setValue(SymbolType.valueOf(company.getType()));
        stockExchange.setValue(StockExchange.valueOf(company.getStockExchange()));
    }

    private void loadBrokerComboBoxData() {
        List<BrokerLightModel> brokers = stocksService.getBrokers();

        broker.setItems(brokers);
        broker.setFilteredItems(brokers);
    }

    public void create(BrokerLightModel brokerLightModel) {
        SymbolViewModel model = SymbolViewModel.of(Symbol.empty());
        model.setBroker(brokerLightModel);
        edit(model);
    }

    public void edit(@NonNull SymbolViewModel viewModel) {
        String code = viewModel.getCode();
        if (code != null) {
            symbolCombo.setItems(CompanyViewModel.of(code));
        }
        loadBrokerComboBoxData();
        loadSymbolsComboData();
        onEdit(viewModel);
    }

    private void loadSymbolsComboData() {
        List<CompanyViewModel> companyViewModels = getSymbolsComboData("");
        symbolCombo.setItems(companyViewModels);
        symbolCombo.setFilteredItems(new TreeSet<>(companyViewModels));
    }

    private List<CompanyViewModel> getSymbolsComboData(@NonNull String symbolSubString) {
        List<CompanyViewModel> models = marketService.getCachedCompanies(symbolSubString).stream()
                .map(CompanyViewModel::of)
                .collect(toList());

        return models;
    }
}