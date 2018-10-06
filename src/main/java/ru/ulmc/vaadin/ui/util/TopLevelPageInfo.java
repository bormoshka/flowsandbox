package ru.ulmc.vaadin.ui.util;

import com.vaadin.flow.component.Component;
import lombok.*;

/**
 * Результат сбора информации по аннотациям @TopLevelPage
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class TopLevelPageInfo implements Comparable<TopLevelPageInfo>{
    private Class<? extends Component> target;
    private String name;
    private String headingText;
    private int order;

    @Override
    public int compareTo(@NonNull TopLevelPageInfo o) {
        return order - o.getOrder();
    }
}
