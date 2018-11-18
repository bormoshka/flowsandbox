package ru.ulmc.investor.ui.view.information.editor;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.RouterLayout;

public abstract class CommonPopupEditor<T> extends Dialog implements RouterLayout {
    Button saveBtn = new Button("Сохранить");
    Button cancelBtn = new Button("Закрыть");
    BeanValidationBinder<T> binder;
    boolean changed = false;
    boolean initialized = false;

    public CommonPopupEditor() {
        addDialogCloseActionListener(this::tryToCloseDialog);
    }

    protected void tryToCloseDialog(Object someEvent) {
        if (changed) {
            showConfirm();
        } else {
            setOpened(false);
        }
    }

    protected H4 getTitle(String text) {
        H4 h4 = new H4(text);
        h4.setWidth("100%");
        return h4;
    }

    protected void init() {
        if (!initialized) {
            initFields();
            initBinder();
            layout();
            initialized = true;
        }
    }

    protected abstract void layout();

    protected abstract void initBinder();

    protected abstract void initFields();

    protected HorizontalLayout getControls() {
        saveBtn.getElement().setAttribute("theme", "primary");
        saveBtn.addClickListener(buttonClickEvent -> {
            if (binder.isValid()) {
                T bean = binder.getBean();
                onSave(bean);
                changed = false;
                tryToCloseDialog(buttonClickEvent);
            }
        });

        cancelBtn.addClickListener(this::tryToCloseDialog);
        HorizontalLayout hl = new HorizontalLayout(saveBtn, cancelBtn);
        hl.setSizeFull();
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return hl;
    }

    protected abstract void onSave(T bean);

    protected void showConfirm() {
        Dialog dialog = new Dialog();
        dialog.add(new Label("Есть несохраненные изменения, точно хотите закрыть окно?"));
        Button close = new Button("Закрыть без изменений");

        close.addClickListener(buttonClickEvent -> {
            dialog.setOpened(false);
            setOpened(false);
        });
        Button proceed = new Button("Вернуться к редактированию");
        proceed.getElement().setAttribute("theme", "primary");
        proceed.addClickListener(buttonClickEvent -> {
            dialog.setOpened(false);
            setOpened(true);
        });
        HorizontalLayout horizontalLayout = new HorizontalLayout(proceed, close);
        horizontalLayout.setSizeFull();
        horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        dialog.add(horizontalLayout);
        dialog.setOpened(true);
    }

    protected void onEdit(T bean) {
        setOpened(true);
        changed = false;
        binder.setBean(bean);
    }
}
