package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox.CustomValueSetEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import ru.ulmc.investor.data.entity.*;
import ru.ulmc.investor.service.MarketService;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.component.CompanyComponent;
import ru.ulmc.investor.ui.entity.BrokerLightModel;
import ru.ulmc.investor.ui.entity.CommonLightModel;
import ru.ulmc.investor.ui.entity.CompanyViewModel;
import ru.ulmc.investor.ui.entity.SymbolViewModel;
import ru.ulmc.investor.ui.util.Notify;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@SpringComponent
@Scope(SCOPE_PROTOTYPE)
@Route(value = "symbol/edit", layout = MainLayout.class)
public class SymbolEditor extends CommonPopupEditor<SymbolViewModel> {
    private final MarketService marketService;
    private final StocksService stocksService;
    private ComboBox<CompanyViewModel> symbolCombo = new ComboBox<>("Код");
    private ComboBox<Currency> positionCurrency = new ComboBox<>("Валюта позиции");
    private ComboBox<Currency> positionCloseCurrency = new ComboBox<>("Валюта закрытия позиции");
    private ComboBox<SymbolType> symbolType = new ComboBox<>("Тип инструмента");
    private ComboBox<StockExchange> stockExchange = new ComboBox<>("Биржа");
    private ComboBox<BrokerLightModel> broker = new ComboBox<>("Брокер");
    private CompanyComponent companyInfo = new CompanyComponent();
    private CompanyViewModel company;
    private HorizontalLayout currencyRow;
    // workaround of https://github.com/vaadin/vaadin-combo-box-flow/issues/167
    private String previousPartialSymbolCode = "";

    @Autowired
    public SymbolEditor(MarketService marketService, StocksService stocksService) {
        this.marketService = marketService;
        this.stocksService = stocksService;
        init();
        setWidth("650px");
    }

    @Override
    protected void layout() {
        HorizontalLayout controls = getControls();

        HorizontalLayout descRow = new HorizontalLayout(symbolCombo, stockExchange);
        descRow.setWidth("100%");
        HorizontalLayout companyRow = new HorizontalLayout(companyInfo);
        companyRow.setWidth("100%");
        currencyRow = new HorizontalLayout(symbolType, positionCurrency, positionCloseCurrency);
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
        binder.bind(symbolType, SymbolViewModel::getType, SymbolViewModel::setType);

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
        symbolCombo.addValueChangeListener(this::onSymbolValueChange);
        symbolCombo.setEnabled(true);
        symbolCombo.setWidth("66.77%");
        symbolCombo.addCustomValueSetListener(this::onCustomSymbolComboBoxValue);
        symbolCombo.setItemLabelGenerator(CompanyViewModel::getSymbol);
    }

    private void onSymbolValueChange(ComponentValueChangeEvent<ComboBox<CompanyViewModel>, CompanyViewModel> event) {
        if (event.isFromClient() && event.getValue() != null) {
            loadCompanyInfo(event.getValue());
        }
    }

    private void onCustomSymbolComboBoxValue(CustomValueSetEvent<ComboBox<CompanyViewModel>> event) {
        final String partialSymbolCode = event.getDetail();
        if (!event.isFromClient()
                || previousPartialSymbolCode.equalsIgnoreCase(partialSymbolCode)) {
            return;
        }
        previousPartialSymbolCode = partialSymbolCode;
        Collection<CompanyViewModel> preSaved = getSymbolsComboData(partialSymbolCode);
        Optional<CompanyViewModel> first = preSaved.stream()
                .filter(company -> company.getSymbol().equalsIgnoreCase(partialSymbolCode))
                .findFirst();
        if (first.isPresent()) {
            loadCompanyInfo(first.get());
        } else {
            loadCompanyInfo(CompanyViewModel.of(partialSymbolCode));
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
        //combo.setFilteredItems(Currency.values());
        combo.setRequired(required);
        combo.setWidth("33.33%");
        combo.setItemLabelGenerator(this::getCurrencyLabel);
        combo.setAllowCustomValue(false);
    }

    private String getCurrencyLabel(Currency item) {
        return item.name() + " (" + item.toString() + ")";
    }

    private void initInstrumentTypeCombo() {
        symbolType.setItems(SymbolType.values());
        symbolType.setValue(SymbolType.STOCK);
        symbolType.setItemLabelGenerator(SymbolType::getDescription);
        symbolType.setAllowCustomValue(false);
        symbolType.setRequired(true);
        symbolType.setWidth("33.33%");
    }

    private void initStockExchangeCombo() {
        stockExchange.setItems(StockExchange.values());
        stockExchange.setValue(StockExchange.UNKNOWN);
        stockExchange.setItemLabelGenerator(StockExchange::getName);
        stockExchange.setRequired(true);
        stockExchange.setAllowCustomValue(false);
        stockExchange.setWidth("33.33%");
    }

    private void enableFields(boolean enabled) {
        symbolType.setEnabled(enabled);
        positionCurrency.setEnabled(enabled);
        positionCloseCurrency.setEnabled(enabled);
        stockExchange.setEnabled(enabled);
        saveBtn.setEnabled(enabled);
    }

    private void loadCompanyInfo(CompanyViewModel symbolCandidate) {
        if (symbolCandidate == null
                || symbolCandidate.getSymbol() == null
                || symbolCandidate.getSymbol().trim().isEmpty()) {
            onCompanyChange(false);
            return;
        }
        if (symbolCandidate.isReliable()) {
            updateFieldsWithCompanyData(symbolCandidate);
            return;
        }
        Optional<CompanyInfo> response = marketService.getCompanyInfo(symbolCandidate.getSymbol());
        if (response.isPresent()) {
            CompanyViewModel company = CompanyViewModel.of(response.get());
            updateFieldsWithCompanyData(company);
            symbolCombo.setInvalid(false);
        } else {
            onCompanyChange(false);
            symbolCombo.setInvalid(true);
            symbolCombo.setErrorMessage("Инструмент с таким кодом не найден");
        }
    }

    private void onCompanyChange(boolean present) {
        enableFields(present);
        currencyRow.setVisible(present);
        companyInfo.setVisible(present);
    }

    private void updateFieldsWithCompanyData(CompanyViewModel company) {
        this.company = company;
        onCompanyChange(true);
        companyInfo.update(company);
        symbolType.setValue(SymbolType.valueOf(company.getType()));
        stockExchange.setValue(StockExchange.valueOf(company.getStockExchange()));
    }

    private void loadBrokerComboBoxData() {
        List<BrokerLightModel> brokers = stocksService.getBrokers();

        broker.setItems(brokers);
        //  broker.setFilteredItems(brokers);
    }

    public void create(BrokerLightModel brokerLightModel) {
        SymbolViewModel model = SymbolViewModel.of(Symbol.empty());
        model.setBroker(brokerLightModel);
        edit(model);
    }

    public void edit(@NonNull SymbolViewModel viewModel) {
        loadBrokerComboBoxData();
        loadSymbolsComboData();

        String code = viewModel.getCode();
        if (code != null) {
            symbolCombo.setValue(CompanyViewModel.of(code));
        }
        onEdit(viewModel);
    }

    private void loadSymbolsComboData() {
        List<CompanyViewModel> companyViewModels = getSymbolsComboData("");
        symbolCombo.setItems(companyViewModels);
    }

    private List<CompanyViewModel> getSymbolsComboData(@NonNull String symbolSubString) {
        return marketService.getCachedCompanies(symbolSubString).stream()
                .map(CompanyViewModel::of)
                .collect(toList());
    }
}