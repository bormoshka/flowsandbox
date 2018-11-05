package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.data.entity.Position;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.PositionViewModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.lang.ThreadLocal.withInitial;
import static java.time.LocalDateTime.now;
import static ru.ulmc.investor.ui.entity.PositionViewModel.toEntity;

@UIScope
@SpringComponent
@Route(value = "positions/close", layout = MainLayout.class)
public class PositionCloseEditor extends PositionEditor {
    private ComboBox<CloseType> closeTypeComboBox;

    private StocksService stocksService;
    private PositionViewModel originalModel;

    public PositionCloseEditor(StocksService stocksService) {
        super(stocksService);
        this.stocksService = stocksService;
        setWidth("650px");
    }

    Component getFirstSelectionComponent() {
        return closeTypeComboBox;
    }

    void initFirstSelectionComponent() {
        closeTypeComboBox = new ComboBox<>("Закрыть позицию");
        closeTypeComboBox.setItems(CloseType.values());
        closeTypeComboBox.setWidth("50%");
        closeTypeComboBox.setAllowCustomValue(false);
        closeTypeComboBox.setAutofocus(true);
        closeTypeComboBox.setItemLabelGenerator(CloseType::getDesc);
        closeTypeComboBox.addValueChangeListener(event -> {
            boolean fullClose = event.getValue() == CloseType.ALL;
            quantity.setEnabled(!fullClose);
            if (fullClose) {
                quantity.setValue(String.valueOf(originalModel.getQuantity()));
            }
        });
        closeTypeComboBox.setValue(CloseType.ALL);
    }

    @Override
    protected void initBinder() {
        binder = new BeanValidationBinder<>(PositionViewModel.class);
        binder.forField(quantity)
                .withValidator((s, b) -> {
                    int newValue = Integer.parseInt(s);
                    if (originalModel.getQuantity() >= newValue && newValue > 0) {
                        return ValidationResult.ok();
                    } else {
                        return ValidationResult.error("Количество для закрытия не должно превышать " +
                                "количество открытой позиции и должно быть больше нуля");
                    }
                })
                .bind(BinderHelper::getSize, BinderHelper::setSize);
        binder.bind(date, BinderHelper::getCloseDate, BinderHelper::setCloseDate);
        binder.bind(time, BinderHelper::getCloseTime, BinderHelper::setCloseTime);
        binder.forField(price)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(BinderHelper::getClosePrice, BinderHelper::setClosePrice);
        binder.forField(currencyPrice)
                .withValidator((value, context) ->
                        getValidationResult(value, "Цена должна быть больше нуля"))
                .bind(BinderHelper::getCurrencyClosePrice, BinderHelper::setCurrencyClosePrice);

        binder.setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        binder.addValueChangeListener(valueChangeEvent -> changed = true);
    }

    @Override
    protected void onSave(PositionViewModel bean) {
        String text = "Позиция \"" + bean.getStockPosition().getName();
        bean.setClosed(true);
        if (bean.getQuantity() == originalModel.getQuantity()) {
            text += "\" успешно закрыта!";
            bean.setId(originalModel.getId());
            stocksService.save(toEntity(bean));
        } else {
            text += "\" успешно частично закрыта!";
            stocksService.closeFractionally(toEntity(originalModel), toEntity(bean));
        }
        Notification.show(text, 2500, Notification.Position.MIDDLE);
    }

    @Override
    public void edit(@NonNull PositionViewModel model) {
        PositionViewModel copy = model.toBuilder().build();
        this.originalModel = model;
        init();
        onEdit(copy);
    }

    @Override
    String getPriceLabel() {
        return "Цена закрытия";
    }

    @Override
    String getDateLabel() {
        return "Дата закрытия";
    }

    @Override
    String getTimeLabel() {
        return "Время закрытия";
    }


    @AllArgsConstructor
    private enum CloseType {
        ALL("Целиком"),
        FRACTIONALLY("Частично");
        @Getter
        private final String desc;
    }

    @Slf4j
    private static class BinderHelper {
        private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        private static final ThreadLocal<NumberFormat> numFormat = withInitial(() -> {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            decimalFormat.setParseBigDecimal(true);
            return decimalFormat;
        });


        @SneakyThrows
        private static void setCurrencyClosePrice(PositionViewModel position, String price) {
            if (isEmpty(price)) {
                return;
            }
            position.setCurrencyClosePrice((BigDecimal) numFormat.get().parse(price));
        }

        private static String getCurrencyClosePrice(PositionViewModel position) {
            BigDecimal price = position.getCurrencyClosePrice();
            if (price != null) {
                return numFormat.get().format(price);
            }
            return null;
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
            if (price != null) {
                return numFormat.get().format(price);
            }
            return null;
        }

        private static String getCloseTime(PositionViewModel position) {
            LocalDateTime date = position.getCloseDate();
            if (date != null) {
                LocalTime from = LocalTime.from(date);
                return df.format(from);
            }
            return null;
        }

        private static void setCloseTime(PositionViewModel position, String time) {
            if (isEmpty(time)) {
                return;
            }
            LocalDateTime date = position.getCloseDate();
            LocalTime parsedTime = LocalTime.from(df.parse(time));
            if (date != null) {
                date = date.with(parsedTime);
            } else {
                date = now().with(parsedTime);
            }
            position.setCloseDate(date);
        }

        private static LocalDate getCloseDate(PositionViewModel position) {
            LocalDateTime closeDate = position.getCloseDate();
            if (closeDate != null) {
                return LocalDate.from(closeDate);
            }
            return LocalDate.now();
        }

        private static void setCloseDate(PositionViewModel position, LocalDate date) {
            if (date == null) {
                return;
            }
            LocalDateTime closeDate = position.getCloseDate();
            if (closeDate != null) {
                closeDate = closeDate.with(date);
            } else {
                closeDate = LocalDateTime.from(date);
            }
            position.setCloseDate(closeDate);
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

        static boolean isEmpty(String s) {
            return s == null || s.trim().isEmpty();
        }

    }
}
