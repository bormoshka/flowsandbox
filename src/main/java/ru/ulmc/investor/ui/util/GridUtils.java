package ru.ulmc.investor.ui.util;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class GridUtils {

    public static Component getRowControls(ComponentEventListener<ClickEvent<Button>>
                                                   onEditListener,
                                           ComponentEventListener<ClickEvent<Button>>
                                                   onRemoveListener) {
        Button editBtn = new Button(new Icon(VaadinIcon.PENCIL));
        editBtn.addClickListener(onEditListener);
        Button removeBtn = new Button(new Icon(VaadinIcon.TRASH));
        removeBtn.addClickListener(onRemoveListener);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        hl.setSizeFull();
        hl.add(editBtn, removeBtn);
        return hl;
    }
}
