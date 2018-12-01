package ru.ulmc.investor.ui.util;


import com.vaadin.flow.component.notification.Notification;

public final class Notify {
    public static void fastToast(String info) {
        Notification.show(info, 1000, Notification.Position.BOTTOM_END);
    }
    public static void info(String info) {
        Notification.show(info, 2500, Notification.Position.MIDDLE);
    }
    public static void error(String errorMsg) {
        Notification.show(errorMsg, 2500, Notification.Position.MIDDLE).open();
    }
}
