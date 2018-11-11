package ru.ulmc.investor.ui.util.encoder;

import com.vaadin.flow.templatemodel.ModelEncoder;
import lombok.SneakyThrows;

import java.math.BigDecimal;

import static ru.ulmc.investor.ui.util.Format.BIG_DECIMAL_SHORT;

public class BigDecimapEncoder implements ModelEncoder<BigDecimal, String> {
    @Override
    public String encode(BigDecimal value) {
        return value.toString();
    }

    @SneakyThrows
    @Override
    public BigDecimal decode(String value) {
        return (BigDecimal) BIG_DECIMAL_SHORT.get().parse(value);
    }
}
