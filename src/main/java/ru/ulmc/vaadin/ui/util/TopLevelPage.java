package ru.ulmc.vaadin.ui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TopLevelPage {

    String menuName();

    int order() default 10000;

    String headingText() default "";

}
