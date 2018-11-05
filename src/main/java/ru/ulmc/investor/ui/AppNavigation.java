package ru.ulmc.investor.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.util.TopLevelPageInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static ru.ulmc.investor.ui.Pages.getPages;

@Slf4j
public class AppNavigation extends Div implements RouterLayout {


    public AppNavigation() {
        log.debug("Starting page info scanner");
        getPages().forEach(this::addPage);
        addClassName("main-layout__nav");
    }

    private void addPage(TopLevelPageInfo page) {
        RouterLink link = new RouterLink(page.getName(), page.getTarget());
        link.addClassName("main-layout__nav-item");
        link.setHighlightCondition(HighlightConditions.sameLocation());
        add(link);
    }
}
