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
public class InstrumentViewModel implements Serializable {
    private Long id;
    private String name;
    private String code;
    private StockExchange stockExchange;
    private InstrumentType type;
    private Currency currency;
    private Currency closeCurrency;

    private BrokerLightModel broker;

    public static InstrumentViewModel of(Instrument stock) {
        Broker brokerEntity = stock.getBroker();
        BrokerLightModel broker = null;
        if(brokerEntity != null) {
             broker = BrokerLightModel.of(brokerEntity);
        }
        return InstrumentViewModel.builder()
                .id(stock.getId())
                .name(stock.getName())
                .code(stock.getCode())
                .type(stock.getType())
                .stockExchange(stock.getStockExchange())
                .currency(stock.getCurrency())
                .closeCurrency(stock.getCloseCurrency())
                .broker(broker)
                .build();
    }
    public static Instrument toEntity(InstrumentViewModel model) {
        Currency closeCurrency = model.getCloseCurrency();
        return Instrument.builder()
                .id(model.getId())
                .name(model.getName())
                .code(model.getCode())
                .type(model.getType())
                .stockExchange(model.getStockExchange())
                .currency(model.getCurrency())
                .closeCurrency(closeCurrency == null ? model.getCurrency() : closeCurrency)
                .broker(BrokerLightModel.toEntity(model.getBroker()))
                .build();
    }
}
