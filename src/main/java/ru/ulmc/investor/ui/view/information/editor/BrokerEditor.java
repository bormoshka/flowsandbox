package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.NonNull;
import ru.ulmc.investor.data.entity.Broker;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;

@UIScope
@SpringComponent
@Route(value = "stocks/broker-edit", layout = MainLayout.class)
public class BrokerEditor extends CommonPopupEditor<Broker> {
    private TextField name;
    private StocksService stocksService;

    public BrokerEditor(StocksService stocksService) {
        this.stocksService = stocksService;
        init();
    }

    @Override
    protected void layout() {
        HorizontalLayout hl = initControls();
        FormLayout form = new FormLayout(name, hl);
        add(form);
    }

    @Override
    protected void initBinder() {
        binder = new BeanValidationBinder<>(Broker.class);
        binder.bind(name, Broker::getName, Broker::setName);
        binder.setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        binder.addValueChangeListener(valueChangeEvent -> changed = true);
    }

    @Override
    protected void onSave(Broker bean) {
        stocksService.save(bean);
        String text = "Портфолио \"" + bean.getName() + "\" успешно сохранено!";
        Notification.show(text, 2500, Notification.Position.MIDDLE);
    }

    @Override
    protected void initFields() {
        name = new TextField("Название");
    }

    public void create() {
        edit(Broker.empty());
    }

    public void edit(@NonNull Broker viewModel) {
        super.onEdit(viewModel);
    }
}
