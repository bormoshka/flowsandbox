package ru.ulmc.investor.ui.entity;

import lombok.*;
import ru.ulmc.investor.data.entity.Broker;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.data.entity.StockExchange;
import ru.ulmc.investor.data.entity.StockPosition;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class StockViewModel implements Serializable {
    private Long id;
    private String name;
    private String code;
    private StockExchange stockExchange;
    private Currency currency;
    private Currency closeCurrency;

    private BrokerLightModel broker;

    public static StockViewModel of(StockPosition stock) {
        Broker brokerEntity = stock.getBroker();
        BrokerLightModel broker = null;
        if(brokerEntity != null) {
             broker = BrokerLightModel.of(brokerEntity);
        }
        return StockViewModel.builder()
                .id(stock.getId())
                .name(stock.getName())
                .code(stock.getCode())
                .stockExchange(stock.getStockExchange())
                .currency(stock.getCurrency())
                .closeCurrency(stock.getCloseCurrency())
                .broker(broker)
                .build();
    }
    public static StockPosition toEntity(StockViewModel model) {
        return StockPosition.builder()
                .id(model.getId())
                .name(model.getName())
                .code(model.getCode())
                .stockExchange(model.getStockExchange())
                .currency(model.getCurrency())
                .closeCurrency(model.getCloseCurrency())
                .broker(BrokerLightModel.toEntity(model.getBroker()))
                .build();
    }
}
