package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.NonNull;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;
import ru.ulmc.investor.ui.entity.PortfolioViewModel;

@UIScope
@SpringComponent
@Route(value = "portfolio/edit", layout = MainLayout.class)
public class PortfolioEditor extends CommonPopupEditor<PortfolioViewModel> {
    private TextField name;
    //private Grid<PositionViewModel> positions;
    private StocksService stocksService;

    public PortfolioEditor(StocksService stocksService) {
        this.stocksService = stocksService;
        init();
        setWidth("350px");
    }

    @Override
    protected void layout() {
        HorizontalLayout hl = getControls();
        FormLayout form = new FormLayout(name);
        form.setWidth("100%");
        VerticalLayout vl = new VerticalLayout(getTitle("Редактирование портфолио:"), form, hl);
        add(vl);
    }

    @Override
    protected void initBinder() {
        binder = new BeanValidationBinder<>(PortfolioViewModel.class);
        binder.bind(name, PortfolioViewModel::getName, PortfolioViewModel::setName);
        binder.setRequiredConfigurator(RequiredFieldConfigurator.NOT_NULL);
        binder.addValueChangeListener(valueChangeEvent -> changed = true);
    }

    @Override
    protected void initFields() {
        name = new TextField("Название");
    }

    @Override
    protected void onSave(PortfolioViewModel bean) {
        stocksService.save(PortfolioViewModel.toEntity(bean));
        String text = "Портфолио \"" + bean.getName() + "\" успешно сохранено!";
        Notification.show(text, 2500, Notification.Position.MIDDLE);
    }

    public void create() {
        edit(new PortfolioViewModel());
    }

    public void edit(@NonNull PortfolioViewModel viewModel) {
        super.onEdit(viewModel);
    }
}
