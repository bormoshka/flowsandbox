package ru.ulmc.investor.ui.entity.position;

import com.vaadin.flow.templatemodel.TemplateModel;

import java.math.BigDecimal;

import lombok.NoArgsConstructor;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;

@NoArgsConstructor
public class PositionPrice implements TemplateModel {
    PositionViewModel model;

    public PositionPrice(PositionViewModel model) {
        this.model = model;
    }

    public String getBaseCurrency() {
        return model.getBaseCurrency();
    }

    public boolean isClosed() {
        return model.isClosed();
    }

    public boolean isLastPriceInitialized() {
        return model.getMarketPrice().isPresent();
    }

    public boolean isOpenWithMarket() {
        return !isClosed() && isLastPriceInitialized();
    }

    public BigDecimal getOpen() {
        return model.getOpenPrice();
    }

    public BigDecimal getClose() {
        return model.getClosePrice();
    }

    public BigDecimal getMarket() {
        return model.getMarketPrice().orElse(ZERO);
    }

    public BigDecimal getProfitPercents() {
        if (isClosed() || isLastPriceInitialized()) {
            return getProfit().multiply(valueOf(100))
                    .divide(this.getOpen().multiply(getSize()), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            return ZERO;
        }

    }

    BigDecimal getSize() {
        return valueOf(model.getQuantity());
    }

    public BigDecimal getProfit() {
        if (isClosed()) {
            return getClose().subtract(getOpen())/*.multiply(getSize())*/;
        } else if (isLastPriceInitialized()) {
            return getMarket().subtract(getOpen())/*.multiply(getSize())*/;
        } else {
            return ZERO;
        }
    }

    public boolean isProfitable() {
        if (isClosed()) {
            return getOpen().compareTo(getClose()) < 0;
        } else if (isLastPriceInitialized()) {
            return getOpen().compareTo(getMarket()) < 0;
        } else {
            return false;
        }
    }

    public String getTrending() {
        return isProfitable() ? "up" : "down";
    }

   /* public String getProfitStatus() {
        if (isClosed() || isOpenWithMarket()) {
            return isProfitable() ? LOSS.getDesc() : PROFIT.getDesc();
        } else {
            return NEUTRAL.getDesc();
        }
    }*/
}
