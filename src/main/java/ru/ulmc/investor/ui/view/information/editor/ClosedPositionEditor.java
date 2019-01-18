package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import ru.ulmc.investor.data.entity.Position;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.position.PositionViewModel;

import static ru.ulmc.investor.ui.entity.position.PositionViewModel.toEntity;

@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Route(value = "positions/close", layout = MainLayout.class)
public class ClosedPositionEditor extends FullPositionEditor {
    private ComboBox<CloseType> closeTypeComboBox;

    private StocksService stocksService;
    private PositionViewModel originalModel;

    public ClosedPositionEditor(StocksService stocksService) {
        super(stocksService);
        this.stocksService = stocksService;
        setWidth("650px");
    }

    @Override
    protected void initBinder() {
        super.initBinder();
        ((PositionBinder)binder).bindQuantity((s, b) -> {
                    int newValue = Integer.parseInt(s);
                    if (originalModel.getQuantity() >= newValue && newValue > 0) {
                        return ValidationResult.ok();
                    } else {
                        return ValidationResult.error("Количество для закрытия не должно превышать " +
                                "количество открытой позиции и должно быть больше нуля");
                    }
                });
    }

    @Override
    protected void onSave(PositionViewModel bean) {
        bean.setClosed(true);
        String text = doClosePosition(bean);
        Notification.show(text, 2500, Notification.Position.MIDDLE);
    }

    private String doClosePosition(PositionViewModel bean) {
        Position entity = toEntity(bean);
        String symName = bean.getSymbol().getName();
        if (bean.isParent()) {
            stocksService.closeAll(entity);
            return "Все позиции " + symName + " были успешно закрыты!";
        }
        String text = "Позиция \"" + symName;
        if (bean.getQuantity() == originalModel.getQuantity()) {
            text += "\" успешно закрыта!";
            bean.setId(originalModel.getId());
            stocksService.save(entity);
        } else {
            text += "\" успешно частично закрыта!";
            stocksService.closeFractionally(toEntity(originalModel), entity);
        }
        return text;
    }

    @Override
    boolean showOpenComponents() {
        return false;
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
    public void edit(@NonNull PositionViewModel model) {
        PositionViewModel copy = model.toBuilder().build();
        this.originalModel = model;
        init();
        onEdit(copy);
        if(model.isParent()) {
            closeTypeComboBox.setValue(CloseType.ALL);
            closeTypeComboBox.setEnabled(false);
        } else {
            closeTypeComboBox.setEnabled(true);
        }
    }

    @AllArgsConstructor
    private enum CloseType {
        ALL("Целиком"),
        FRACTIONALLY("Частично");
        @Getter
        private final String desc;
    }
}