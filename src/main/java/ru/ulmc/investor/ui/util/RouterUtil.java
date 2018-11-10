package ru.ulmc.investor.ui.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Router;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class RouterUtil {

    public static <T extends Component & HasUrlParameter<String>> void navigateTo(
            Class<T> routeTarget, String... params) {

        UI current = UI.getCurrent();
        Router router = current.getRouter();
        String url = router.getUrl(routeTarget, Arrays.asList(params));
        current.navigate(url);

    }

    public static String collectParams(String... params) {
        return Stream.of(params)
                .map(HtmlUtils::htmlEscape)
                .collect(joining("/"));
    }


    public static List<String> unescapeParams(String paramsString) {
        return Stream.of(paramsString.split("/"))
                .map(HtmlUtils::htmlUnescape)
                .collect(toList());
    }

}
