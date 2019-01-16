package ru.ulmc.investor.ui.component;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ConfirmDialog extends Dialog {
    private boolean proceed = false;

    public ConfirmDialog(String text, Runnable onProceed, Runnable onCancel) {
        Button confirm = new Button("Продолжить");
        confirm.addClickListener(e -> {
            proceed = true;
            onProceed.run();
            close();
        });

        Button cancel = new Button("Отмена");
        cancel.addClickListener(e -> close());
        HorizontalLayout hl = new HorizontalLayout(confirm, cancel);
        add(new VerticalLayout(new Label(text), hl));
        addDialogCloseActionListener(dialogCloseActionEvent -> {
            if (!proceed) {
                onCancel.run();
            }
        });
        setOpened(true);
    }

    public static ConfirmDialog show(String text, Runnable onProceed) {
        return show(text, onProceed, () -> {
        });
    }

    public static ConfirmDialog show(String text, Runnable onProceed, Runnable onCancel) {
        return new ConfirmDialog(text, onProceed, onCancel);
    }
}
