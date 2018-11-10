package ru.ulmc.investor.data.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

import static ru.ulmc.investor.data.entity.StockExchange.MCX;

@Entity
@Table(name = "CI_INSTRUMENTS")

@Getter
@Setter
@ToString(exclude = {"positions"})
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Наименование позиции
     */
    @NonNull
    @Column(nullable = false)
    private String name;
    /**
     * Условное обозначние позиции
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private String code = "";

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "BROKER_ID", nullable = true)
    private Broker broker;

    @OneToMany(mappedBy = "instrument", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
    private List<Position> positions;

    /**
     * Код биржи
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private StockExchange stockExchange = MCX;

    /**
     * Валюта позиции
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Currency currency = Currency.RUB;

    /**
     * Валюта позиции
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private InstrumentType type = InstrumentType.IRRELEVANT;


    /**
     * Валюта, в которую будет осуществлен пересчет при закрытии позиции.
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Currency closeCurrency = Currency.RUB;


    public static Instrument empty() {
        return Instrument.builder()
                .code("")
                .type(InstrumentType.IRRELEVANT)
                .name("Новая позиция")
                .closeCurrency(Currency.RUB)
                .currency(Currency.RUB)
                .stockExchange(MCX)
                .build();
    }
}
