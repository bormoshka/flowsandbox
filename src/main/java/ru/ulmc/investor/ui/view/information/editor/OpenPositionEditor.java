package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.MainLayout;

import static java.time.LocalDateTime.now;

@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Route(value = "positions/open", layout = MainLayout.class)
public class OpenPositionEditor extends FullPositionEditor {

    public OpenPositionEditor(StocksService stocksService) {
        super(stocksService);
        setWidth("650px");
    }

    @Override
    boolean showClosedComponents() {
        return false;
    }
}
