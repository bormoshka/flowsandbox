package ru.ulmc.investor.ui.entity;


import com.vaadin.flow.templatemodel.TemplateModel;
import lombok.*;
import ru.ulmc.investor.data.entity.CompanyInfo;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"symbol"})
public class CompanyViewModel implements TemplateModel, Comparable<CompanyViewModel> {
    private String name;

    @NonNull
    private String symbol;

    private String description;

    private String industry;

    private String sector;

    private String ceo;
    private String type;
    private String stockExchange;

    private boolean reliable = true;

    public static CompanyViewModel of(String symbol) {
        CompanyViewModel model = new CompanyViewModel();
        model.symbol = symbol;
        model.reliable = false;
        return model;
    }

    public static CompanyViewModel of(CompanyInfo info) {
        CompanyViewModel model = new CompanyViewModel();
        model.symbol = info.getSymbol();
        model.description = info.getDescription();
        model.name = info.getName();
        model.industry = info.getIndustry();
        model.sector = info.getSector();
        model.ceo = info.getCeo();
        model.type = info.getType().name();
        model.stockExchange = info.getStockExchange().name();
        model.reliable = true;
        return model;
    }

    @Override
    public int compareTo(CompanyViewModel o) {
        return this.symbol.compareTo(o.getSymbol());
    }
}
