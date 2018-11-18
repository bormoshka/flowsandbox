package ru.ulmc.investor.ui.component;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import ru.ulmc.investor.ui.entity.CompanyViewModel;

@Tag("company-info")
@HtmlImport("frontend://src/market/company-info.html")
public class CompanyComponent extends PolymerTemplate<CompanyViewModel> {

    public CompanyComponent() {
        setId("company-info");

    }

    public void update(CompanyViewModel model) {
        getModel().setName(model.getName());
        getModel().setDescription(model.getDescription());
        getModel().setCeo(model.getCeo());
        getModel().setSector(model.getSector());
        getModel().setIndustry(model.getIndustry());
    }

}