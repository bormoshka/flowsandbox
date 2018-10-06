package ru.ulmc.vaadin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.vaadin.ui.util.TopLevelPage;
import ru.ulmc.vaadin.ui.util.TopLevelPageInfo;
import ru.ulmc.vaadin.ui.view.CommonPage;

import java.util.*;

@Slf4j
public class AppNavigation extends Div implements RouterLayout {

    private final Collection<TopLevelPageInfo> topLevelPageInfos;

    public AppNavigation() {
        topLevelPageInfos = scanForViews();
        topLevelPageInfos.forEach(page -> {
            RouterLink link = new RouterLink(page.getName(), page.getTarget());
            link.setHighlightCondition(HighlightConditions.sameLocation());
            add(link);
        });


    }

    private Collection<TopLevelPageInfo> scanForViews() { // todo: make it static
        String pkg = "ru.ulmc.vaadin.ui.view";
        List<TopLevelPageInfo> topLevelPageInfos = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().whitelistPackages(pkg).scan()) {
            String canonicalName = TopLevelPage.class.getCanonicalName();
            scanResult.getClassesWithAnnotation(canonicalName).forEach(classInfo -> {
                AnnotationInfo topLevelInfo = classInfo.getAnnotationInfo(canonicalName);
                List<AnnotationParameterValue> topLevelParams = topLevelInfo.getParameterValues();
                String name = "";
                String headingText = "";
                int order = 0;
                for (AnnotationParameterValue aParam : topLevelParams) {
                    String pName = aParam.getName();
                    switch (pName) {
                        case "menuName":
                            name = (String) aParam.getValue();
                            break;
                        case "headingText":
                            headingText = (String) aParam.getValue();
                            break;
                        case "order":
                            order = (int) aParam.getValue();
                            break;
                    }
                }
                //AnnotationInfo routeInfo = classInfo.getAnnotationInfo(Route.class.getCanonicalName());
                //if (routeInfo == null) {
                //    log.warn("Misconfiguration of {}: missing @Route!", classInfo.getSimpleName());
                //    return;
                //}
                //AnnotationParameterValue value = routeInfo.getParameterValues().stream().filter(pv -> pv.getName().equals("value"))
                //        .findFirst()
                //        .orElse(null);
                //if (value == null) {
                //    log.warn("Misconfiguration of {}: @Route missing \"value\" param!", classInfo.getSimpleName());
                //    return;
                //}
                topLevelPageInfos.add(TopLevelPageInfo.builder()
                        .name(name)
                        .headingText(headingText)
                        .order(order)
                        .target((Class<? extends Component>) classInfo.loadClass()).build());
                log.info("Found view {} adding to app menu", classInfo.getSimpleName());
            });
        }
        Collections.sort(topLevelPageInfos);
        return topLevelPageInfos;
    }
}
