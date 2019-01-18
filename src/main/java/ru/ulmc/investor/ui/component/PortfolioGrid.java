package ru.ulmc.investor.ui.component;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ulmc.investor.service.StocksService;
import ru.ulmc.investor.ui.entity.PortfolioViewModel;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@UIScope
public class PortfolioGrid extends Grid<PortfolioViewModel> {

    private Consumer<PortfolioViewModel> onEditCallback;
    @Autowired
    private StocksService service;

    public PortfolioGrid() {
        setSizeFull();
    }

    @PostConstruct
    protected void init() {
        getHeaderRows().remove(0);
        addColumn(PortfolioViewModel::getName);
        addColumn(PortfolioViewModel::getName);
        addComponentColumn(portfolioViewModel -> {
            Button btn = new Button("Редактирвать");
            btn.addClickListener(buttonClickEvent -> onEditCallback.accept(portfolioViewModel));
            return btn;
        });
    }

    public void loadData() {

    }

}
