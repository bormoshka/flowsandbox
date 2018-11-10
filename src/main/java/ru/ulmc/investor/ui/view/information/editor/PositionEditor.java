package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.data.entity.Instrument;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.PortfolioLightModel;
import ru.ulmc.investor.ui.entity.PositionViewModel;
import ru.ulmc.investor.ui.entity.InstrumentViewModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static java.lang.ThreadLocal.withInitial;
import static java.time.LocalDateTime.now;
import static ru.ulmc.investor.ui.util.UiUtils.getCalendarI18n;

@UIScope
@SpringComponent
@Route(value = "positions/edit", layout = MainLayout.class)
public class PositionEditor extends CommonPopupEditor<PositionViewModel> {
    private static final Locale LOCALE = new Locale("ru", "RU");
    private static final String DECIMAL_PATTERN = "\\d+(\\.\\d{1,10})?";

    TextField quantity = new TextField("Количество");
    TextField price = new TextField(getPriceLabel());
    TextField currencyPrice = new TextField("Цена валюты");
    DatePicker date = new DatePicker(getDateLabel());
    TextField time = new TextField(getTimeLabel());
    private ComboBox<InstrumentViewModel> stockComboBox = new ComboBox<>("Позиция");
    private int fieldWidth = 200;
    private StocksService stocksService;

    public PositionEditor(StocksService stocksService) {
        this.stocksService = stocksService;
        setWidth("650px");
    }

    @Override
    protected void layout() {
        HorizontalLayout controls = initControls();

        HorizontalLayout nameLine = new HorizontalLayout(getFirstSelectionComponent(), quantity);
        nameLine.setWidth("100%");
        HorizontalLayout priceLine = new HorizontalLayout(price, currencyPrice);
        priceLine.setWidth("100%");
        HorizontalLayout timeLine = new HorizontalLayout(date, time);
        timeLine.setWidth("100%");

        VerticalLayout verticalLayout = new VerticalLayout(nameLine, priceLine, timeLine, controls);
        verticalLayout.setSizeFull();
        add(verticalLayout);
    }

    Component getFirstSelectionComponent() {
        return stockComboBox;
    }

    @Override
    protected void initBinder() {
        binder = new BeanValidationBinder<>(PositionViewModel.class);
        binder.forField(quantity)
                .withValidator((s, b) ->
                        getValidationResult(s, "Количество у открытой позиции должно быть больше нуля"))
                .bind(BinderHelper::getSize, BinderHelper::setSize);
        binder.forField(date)
                .asRequired()
                .bind(BinderHelper::getOpenDate, BinderHelper::setOpenDate);
        binder.bind(time, BinderHelper::getOpenTime, BinderHelper::setOpenTime);
        binder.forField(price)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(BinderHelper::getOpenPrice, BinderHelper::setOpenPriceToBean);
        binder.forField(currencyPrice)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(BinderHelper::getCurrencyOpenPrice, BinderHelper::setCurrencyOpenPrice);
        binder.forField(stockComboBox)
                .asRequired()
                .bind(BinderHelper::getStock, BinderHelper::setStock);

        binder.setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        binder.addValueChangeListener(valueChangeEvent -> changed = true);
    }

    ValidationResult getValidationResult(String s, String errorMessage) {
        boolean notEmpty = s != null && !s.isEmpty();
        if (notEmpty && Integer.parseInt(s) > 0) {
            return ValidationResult.ok();
        } else {
            return ValidationResult.error(errorMessage);
        }
    }

    @Override
    protected void onSave(PositionViewModel bean) {
        stocksService.save(PositionViewModel.toEntity(bean));
        String text = "Позиция \"" + bean.getInstrument().getName() + "\" успешно сохранена!";
        Notification.show(text, 2500, Notification.Position.MIDDLE);
    }

    @Override
    protected void initFields() {
        quantity.setPattern("[0-9]*");
        quantity.setPreventInvalidInput(true);
        quantity.setWidth("50%");
        quantity.setRequired(true);

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

        initFirstSelectionComponent();
    }

    void initFirstSelectionComponent() {
        stockComboBox.setWidth("50%");
        stockComboBox.setAutofocus(true);
        stockComboBox.setAllowCustomValue(false);
        stockComboBox.setItemLabelGenerator(InstrumentViewModel::getName);
        stockComboBox.setRenderer(TemplateRenderer.<InstrumentViewModel>of(getTemplate())
                .withProperty("name", InstrumentViewModel::getName)
                .withProperty("currency", InstrumentViewModel::getCurrency)
                .withProperty("currencyClose", InstrumentViewModel::getCloseCurrency)
                .withProperty("broker", stockViewModel -> stockViewModel.getBroker().getName())
                .withProperty("code", InstrumentViewModel::getCode));
    }

    private String getTemplate() {
        return "<div class='position-combo'>[[item.name]]<br>" +
                "<span class='sub-text'>[[[item.code]]] | [[item.currency]]/[[item.currencyClose]] " +
                "| <span class='broker'>[[item.broker]]</span>" +
                "</span></div>";
    }

    private void loadStockData() {
        List<InstrumentViewModel> stockPositions = stocksService.getStockPositions();
        stockComboBox.setFilteredItems(stockPositions);
        stockComboBox.setItems(stockPositions);
    }

    public void create(@NonNull PortfolioLightModel parent) {
        edit(PositionViewModel.builder()
                .portfolio(parent)
                .instrument(InstrumentViewModel.of(Instrument.empty()))
                .build());
    }

    public void edit(@NonNull PositionViewModel viewModel) {
        init();
        loadStockData();
        onEdit(viewModel);
    }

    String getPriceLabel() {
        return "Цена открытия";
    }

    String getDateLabel() {
        return "Дата открытия";
    }

    String getTimeLabel() {
        return "Время открытия";
    }


    @Slf4j
    static class BinderHelper {
        static final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        static final ThreadLocal<NumberFormat> numFormat = withInitial(() -> {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            decimalFormat.setParseBigDecimal(true);
            return decimalFormat;
        });


        @SneakyThrows
        static void setCurrencyOpenPrice(PositionViewModel position, String price) {
            if (isEmpty(price)) {
                return;
            }
            position.setCurrencyOpenPrice((BigDecimal) numFormat.get().parse(price));
        }

        static String getCurrencyOpenPrice(PositionViewModel position) {
            BigDecimal openPrice = position.getCurrencyOpenPrice();
            if (openPrice != null) {
                return numFormat.get().format(openPrice);
            }
            return null;
        }

        @SneakyThrows
        static void setOpenPriceToBean(PositionViewModel position, String price) {
            if (isEmpty(price)) {
                return;
            }
            position.setOpenPrice((BigDecimal) numFormat.get().parse(price));
        }

        static String getOpenPrice(PositionViewModel position) {
            BigDecimal openPrice = position.getOpenPrice();
            if (openPrice != null) {
                return numFormat.get().format(openPrice);
            }
            return null;
        }

        static String getOpenTime(PositionViewModel position) {
            LocalDateTime openDate = position.getOpenDate();
            if (openDate != null) {
                LocalTime from = LocalTime.from(openDate);
                return df.format(from);
            }
            return null;
        }

        static void setOpenTime(PositionViewModel position, String time) {
            if (isEmpty(time)) {
                return;
            }
            LocalDateTime openDate = position.getOpenDate();
            LocalTime parsedTime = LocalTime.from(df.parse(time));
            if (openDate != null) {
                openDate = openDate.with(parsedTime);
            } else {
                openDate = now().with(parsedTime);
            }
            position.setOpenDate(openDate);
        }

        static LocalDate getOpenDate(PositionViewModel position) {
            LocalDateTime openDate = position.getOpenDate();
            if (openDate != null) {
                return LocalDate.from(openDate);
            }
            LocalDate now = LocalDate.now();
            setOpenDate(position, now);
            return now;
        }

        static void setOpenDate(PositionViewModel position, LocalDate date) {
            if (date == null) {
                return;
            }
            LocalDateTime openDate = position.getOpenDate();
            if (openDate != null) {
                openDate = openDate.with(date);
            } else {
                openDate = LocalDateTime.of(date, LocalTime.MIDNIGHT);
            }
            position.setOpenDate(openDate);
        }

        static void setSize(PositionViewModel position, String s) {
            if (isEmpty(s)) {
                return;
            }
            position.setQuantity(Integer.parseInt(s));
        }

        static String getSize(PositionViewModel position) {
            return String.valueOf(position.getQuantity());
        }

        static void setStock(PositionViewModel position, InstrumentViewModel stock) {
            if (stock == null) {
                return;
            }
            position.setInstrument(stock);
        }

        static InstrumentViewModel getStock(PositionViewModel position) {
            if (position.getInstrument().getId() == null) {
                return null;
            }
            return position.getInstrument();
        }

        static boolean isEmpty(String s) {
            return s == null || s.trim().isEmpty();
        }
    }
}
