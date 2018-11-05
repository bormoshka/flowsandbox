package ru.ulmc.investor.ui.entity;

import lombok.Getter;
import lombok.Setter;
import ru.ulmc.investor.data.entity.Broker;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class BrokerLightModel extends CommonLightModel {

    BrokerLightModel(Broker broker) {
        this.id = broker.getId();
        this.name = broker.getName();
    }


    public static BrokerLightModel of(@NotNull Broker broker) {
        return new BrokerLightModel(broker);
    }

    public static Broker toEntity(@NotNull BrokerLightModel broker) {
        return Broker.builder()
                .id(broker.id)
                .name(broker.name)
                .build();
    }
}
