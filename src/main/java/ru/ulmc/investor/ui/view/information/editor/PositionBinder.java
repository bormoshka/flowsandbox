package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.ui.entity.InstrumentViewModel;
import ru.ulmc.investor.ui.entity.PositionViewModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.ThreadLocal.withInitial;
import static java.time.LocalDateTime.now;

@Slf4j
class PositionBinder extends BeanValidationBinder<PositionViewModel> {
    static final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
    static final ThreadLocal<NumberFormat> numFormat = withInitial(() -> {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    });
    private FullPositionEditor editor;

    public PositionBinder(FullPositionEditor editor) {
        super(PositionViewModel.class);
        this.editor = editor;
        initBinder();
    }

    @SneakyThrows
    private static void setCurrencyOpenPrice(PositionViewModel position, String price) {
        if (isEmpty(price)) {
            return;
        }
        position.setCurrencyOpenPrice((BigDecimal) numFormat.get().parse(price));
    }

    private static String getCurrencyOpenPrice(PositionViewModel position) {
        BigDecimal openPrice = position.getCurrencyOpenPrice();
        return getStringFromPrice(openPrice);
    }

    private static String getStringFromPrice(BigDecimal openPrice) {
        if (openPrice != null) {
            return numFormat.get().format(openPrice);
        }
        return null;
    }

    @SneakyThrows
    private static void setOpenPriceToBean(PositionViewModel position, String price) {
        if (isEmpty(price)) {
            return;
        }
        position.setOpenPrice((BigDecimal) numFormat.get().parse(price));
    }

    private static String getOpenPrice(PositionViewModel position) {
        BigDecimal openPrice = position.getOpenPrice();
        return getStringFromPrice(openPrice);
    }

    private static void setSize(PositionViewModel position, String s) {
        if (isEmpty(s)) {
            return;
        }
        position.setQuantity(Integer.parseInt(s));
    }

    private static String getSize(PositionViewModel position) {
        return String.valueOf(position.getQuantity());
    }

    private static void setStock(PositionViewModel position, InstrumentViewModel stock) {
        if (stock == null) {
            return;
        }
        position.setInstrument(stock);
    }

    private static InstrumentViewModel getStock(PositionViewModel position) {
        if (position.getInstrument().getId() == null) {
            return null;
        }
        return position.getInstrument();
    }

    @SneakyThrows
    private static void setCurrencyClosePrice(PositionViewModel position, String price) {
        if (isEmpty(price)) {
            return;
        }
        position.setCurrencyClosePrice((BigDecimal) numFormat.get().parse(price));
    }

    private static String getCurrencyClosePrice(PositionViewModel position) {
        BigDecimal price = position.getCurrencyClosePrice();
        return getStringFromPrice(price);
    }

    @SneakyThrows
    private static void setClosePrice(PositionViewModel position, String price) {
        if (isEmpty(price)) {
            return;
        }
        position.setClosePrice((BigDecimal) numFormat.get().parse(price));
    }

    private static String getClosePrice(PositionViewModel position) {
        BigDecimal price = position.getClosePrice();
        return getStringFromPrice(price);
    }

    private static String getOpenTime(PositionViewModel position) {
        LocalDateTime openDate = position.getOpenDate();
        return getStringFromTime(openDate);
    }

    private static void setOpenTime(PositionViewModel position, String time) {
        addTimeToDateTime(time, position, PositionViewModel::getOpenDate, PositionViewModel::setOpenDate);
    }

    private static LocalDate getOpenDate(PositionViewModel position) {
        LocalDateTime openDate = position.getOpenDate();
        if (openDate != null) {
            return LocalDate.from(openDate);
        }
        LocalDate now = LocalDate.now();
        setOpenDate(position, now);
        return now;
    }

    private static void setOpenDate(PositionViewModel position, LocalDate date) {
        addDateToDateTime(date, position, PositionViewModel::getOpenDate, PositionViewModel::setOpenDate);
    }

    private static String getCloseTime(PositionViewModel position) {
        LocalDateTime date = position.getCloseDate();
        return getStringFromTime(date);
    }

    private static void setCloseTime(PositionViewModel position, String time) {
        addTimeToDateTime(time, position, PositionViewModel::getCloseDate, PositionViewModel::setCloseDate);
    }

    private static LocalDate getCloseDate(PositionViewModel position) {
        LocalDateTime closeDate = position.getCloseDate();
        if (closeDate != null) {
            return LocalDate.from(closeDate);
        }
        LocalDate now = LocalDate.now();
        setCloseDate(position, now);
        return LocalDate.now();
    }

    private static void setCloseDate(PositionViewModel position, LocalDate date) {
        addDateToDateTime(date, position, PositionViewModel::getCloseDate, PositionViewModel::setCloseDate);
    }

    private static void addDateToDateTime(LocalDate date,
                                          PositionViewModel position,
                                          Function<PositionViewModel, LocalDateTime> getter,
                                          BiConsumer<PositionViewModel, LocalDateTime> setter) {
        if (date == null) {
            return;
        }
        LocalDateTime dateTime = getter.apply(position);
        if (dateTime != null) {
            dateTime = dateTime.with(date);
        } else {
            dateTime = date.atStartOfDay();
        }
        setter.accept(position, dateTime);
    }

    private static void addTimeToDateTime(String time,
                                          PositionViewModel position,
                                          Function<PositionViewModel, LocalDateTime> getter,
                                          BiConsumer<PositionViewModel, LocalDateTime> setter) {
        if (isEmpty(time)) {
            return;
        }
        LocalDateTime dateTime = getter.apply(position);
        LocalTime parsedTime = LocalTime.from(df.parse(time));
        if (dateTime != null) {
            dateTime = dateTime.with(parsedTime);
        } else {
            dateTime = now().with(parsedTime);
        }
        setter.accept(position, dateTime);
    }

    private static String getStringFromTime(LocalDateTime date) {
        if (date != null) {
            LocalTime from = LocalTime.from(date);
            return df.format(from);
        }
        return null;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    void bindQuantity(Validator<? super String> validator) {
        forField(editor.quantity)
                .withValidator(validator)
                .bind(PositionBinder::getSize, PositionBinder::setSize);
    }

    protected void initBinder() {


        if (editor.showOpenComponents()) {
            bindQuantity((s, b) -> getValidationResult(s, "Количество у позиции должно быть больше нуля"));
            initOpenFieldsBinder();
        }

        if (editor.showClosedComponents()) {
            initClosedFieldsBinder();
        }

        setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        addValueChangeListener(valueChangeEvent -> editor.changed = true);
    }

    private void initOpenFieldsBinder() {
        forField(editor.date)
                .asRequired()
                .bind(PositionBinder::getOpenDate, PositionBinder::setOpenDate);
        bind(editor.time, PositionBinder::getOpenTime, PositionBinder::setOpenTime);
        forField(editor.price)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(PositionBinder::getOpenPrice, PositionBinder::setOpenPriceToBean);
        forField(editor.currencyPrice)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(PositionBinder::getCurrencyOpenPrice, PositionBinder::setCurrencyOpenPrice);
        forField(editor.stockComboBox)
                .asRequired()
                .bind(PositionBinder::getStock, PositionBinder::setStock);
    }

    private void initClosedFieldsBinder() {
        bind(editor.dateClosed, PositionBinder::getCloseDate, PositionBinder::setCloseDate);
        bind(editor.timeClosed, PositionBinder::getCloseTime, PositionBinder::setCloseTime);
        forField(editor.priceClosed)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(PositionBinder::getClosePrice, PositionBinder::setClosePrice);
        forField(editor.currencyPriceClosed)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(PositionBinder::getCurrencyClosePrice, PositionBinder::setCurrencyClosePrice);
    }

    ValidationResult getValidationResult(String s, String errorMessage) {
        boolean notEmpty = s != null && !s.isEmpty();
        if (notEmpty && Integer.parseInt(s) > 0) {
            return ValidationResult.ok();
        } else {
            return ValidationResult.error(errorMessage);
        }
    }
}
