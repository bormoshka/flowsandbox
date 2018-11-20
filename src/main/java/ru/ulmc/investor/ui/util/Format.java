package ru.ulmc.investor.ui.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.lang.ThreadLocal.withInitial;

public class Format {

    public static final Locale LOCALE = new Locale("ru", "RU");
    public static final String DECIMAL_PATTERN = "\\d+(\\.\\d{1,10})?";

    public static final DateTimeFormatter HOURS_MINUTES_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static final ThreadLocal<NumberFormat> BIG_DECIMAL_FORMAT = withInitial(() -> {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(LOCALE));
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    });
    public static final ThreadLocal<NumberFormat> BIG_DECIMAL_SHORT = withInitial(() -> {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    });
}
