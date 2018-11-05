package ru.ulmc.investor.ui.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.ulmc.investor.data.entity.Portfolio;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PortfolioLightModel extends CommonLightModel {

    PortfolioLightModel(Portfolio portfolio) {
        this.id = portfolio.getId();
        this.name = portfolio.getName();
    }

    public static PortfolioLightModel of(Portfolio portfolio) {
        return new PortfolioLightModel(portfolio);
    }

    public static Portfolio toEntity(PortfolioLightModel portfolio) {
        return new Portfolio(portfolio.getId(), portfolio.getName());
    }
}
