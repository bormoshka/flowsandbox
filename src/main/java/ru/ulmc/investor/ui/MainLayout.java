package ru.ulmc.investor.ui;

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.*;

@HtmlImport("frontend://styles/shared-styles.html")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class MainLayout extends Div implements RouterLayout {

    public MainLayout() {
        H2 title = new H2("SandBox Application");
        title.addClassName("main-layout__title"); // Я уже ненавижу BEM

        AppNavigation appNavigation = new AppNavigation();

        Div header = new Div(title, appNavigation);
        header.addClassName("main-layout__header");
        add(header);

        addClassName("main-layout");
        setSizeFull();
    }
}
