package ru.ulmc.investor.ui;

import com.vaadin.flow.component.Component;
import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.ui.util.TopLevelPage;
import ru.ulmc.investor.ui.util.TopLevelPageInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Pages {
    private static final List<TopLevelPageInfo> pages = new ArrayList<>();

    public static Collection<TopLevelPageInfo> getPages() {
        if (pages.isEmpty()) {
            synchronized (pages) {
                String pkg = "ru.ulmc.investor.ui.view";
                try (ScanResult scanResult = new ClassGraph().enableAllInfo().whitelistPackages(pkg).scan()) {
                    String canonicalName = TopLevelPage.class.getCanonicalName();
                    scanResult.getClassesWithAnnotation(canonicalName).forEach(classInfo -> {
                        pages.add(extractPageInfo(canonicalName, classInfo));
                    });
                }
                Collections.sort(pages);
            }
        }
        return pages;
    }

    @SuppressWarnings("unchecked")
    private static TopLevelPageInfo extractPageInfo(String canonicalName, ClassInfo classInfo) {
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
        log.info("Found view {} adding to app menu", classInfo.getSimpleName());
        return TopLevelPageInfo.builder()
                .name(name)
                .headingText(headingText)
                .order(order)
                .target((Class<? extends Component>) classInfo.loadClass()).build();
    }
}
