package ru.ulmc.investor.ui;

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.material.Material;

//@Theme(value = Material.class, variant = Material.DARK)
@Push
@Theme(value = Lumo.class, variant = Lumo.DARK)
@HtmlImport("frontend://styles/shared-styles.html")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class MainLayout extends Div implements RouterLayout {

    public MainLayout() {
        H2 title = new H2("Couch Investor");
        title.addClassName("root-layout__title"); // Я уже ненавижу BEM

        AppNavigation appNavigation = new AppNavigation();

        Div header = new Div(title, appNavigation);
        header.addClassName("root-layout__header"); // Меньшее из зол
        add(header);

        addClassName("root-layout");
        setSizeFull();
    }
}
