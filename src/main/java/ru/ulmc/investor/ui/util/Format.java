package ru.ulmc.investor.ui.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.ThreadLocal.withInitial;

public class Format {
    public static final ThreadLocal<NumberFormat> BIG_DECIMAL_SHORT = withInitial(() -> {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    });
}
