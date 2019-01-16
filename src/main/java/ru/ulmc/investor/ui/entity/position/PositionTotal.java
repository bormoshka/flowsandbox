package ru.ulmc.investor.ui.entity.position;

import java.math.BigDecimal;

import lombok.NoArgsConstructor;

import static java.math.BigDecimal.ZERO;

@NoArgsConstructor
public class PositionTotal extends PositionPrice {
    public PositionTotal(PositionViewModel model) {
        super(model);
    }

    @Override
    public BigDecimal getOpen() {
        return model.getOpenPrice().multiply(getSize());
    }

    @Override
    public BigDecimal getClose() {
        if (model.getClosePrice() == null) {
            return null;
        }
        return model.getClosePrice().multiply(getSize());
    }

    @Override
    public BigDecimal getMarket() {
        if (isLastPriceInitialized()) {
            return getMarket().multiply(getSize());
        }
        return ZERO;
    }

}
