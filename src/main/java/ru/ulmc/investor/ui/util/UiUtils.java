package ru.ulmc.investor.ui.util;

import com.vaadin.flow.component.datepicker.DatePicker;

import static java.util.Arrays.asList;

public class UiUtils {

    public static final DatePicker.DatePickerI18n I_18_N = new DatePicker.DatePickerI18n()
            .setFirstDayOfWeek(0)
            .setMonthNames(asList("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь",
                    "Октябрь", "Ноябрь", "Декабрь"))
            .setToday("Сегодня")
            .setWeek("Неделя")
            .setWeekdays(asList("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"))
            .setWeekdaysShort(asList("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"))
            .setCalendar("Календарь")
            .setCancel("Отмена")
            .setClear("Очитсить");

    public static DatePicker.DatePickerI18n getCalendarI18n() {
        return I_18_N;
    }
}
