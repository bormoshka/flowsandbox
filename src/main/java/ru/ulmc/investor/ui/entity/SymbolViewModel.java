package ru.ulmc.investor.ui.entity;

import lombok.*;
import ru.ulmc.investor.data.entity.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class SymbolViewModel implements Serializable {
    private Long id;
    private String name;
    private String code;
    private StockExchange stockExchange;
    private SymbolType type;
    private Currency currency;
    private Currency closeCurrency;

    private BrokerLightModel broker;

    public static SymbolViewModel of(Symbol stock) {
        Broker brokerEntity = stock.getBroker();
        BrokerLightModel broker = null;
        if(brokerEntity != null) {
             broker = BrokerLightModel.of(brokerEntity);
        }
        return SymbolViewModel.builder()
                .id(stock.getId())
                .name(stock.getName())
                .code(stock.getSymbol())
                .type(stock.getType())
                .stockExchange(stock.getStockExchange())
                .currency(stock.getCurrency())
                .closeCurrency(stock.getCloseCurrency())
                .broker(broker)
                .build();
    }
    public static Symbol toEntity(SymbolViewModel model) {
        Currency closeCurrency = model.getCloseCurrency();
        return Symbol.builder()
                .id(model.getId())
                .name(model.getName())
                .symbol(model.getCode())
                .type(model.getType())
                .stockExchange(model.getStockExchange())
                .currency(model.getCurrency())
                .closeCurrency(closeCurrency == null ? model.getCurrency() : closeCurrency)
                .broker(BrokerLightModel.toEntity(model.getBroker()))
                .build();
    }
}
