package ru.ulmc.investor.data.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

import static ru.ulmc.investor.data.entity.StockExchange.MCX;

@Entity
@Table(name = "CI_SYMBOLS")

@Getter
@Setter
@ToString(exclude = {"positions"})
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class Symbol {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Наименование инструмента.
     */
    @NonNull
    @Column(nullable = false)
    private String name;

    /**
     * Условное обозначение инструмента.
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private String symbol = "";

    /**
     * Брокер, предоставляющий доступ к бирже, где торгуется инструмент.
     */
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "BROKER_ID", nullable = true)
    private Broker broker;

    @OneToMany(mappedBy = "symbol", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
    private List<Position> positions;

    /**
     * Код биржи.
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private StockExchange stockExchange = MCX;

    /**
     * Валюта позиции.
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Currency currency = Currency.RUB;

    /**
     * Валюта позиции.
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private SymbolType type = SymbolType.IRRELEVANT;


    /**
     * Валюта, в которую будет осуществлен пересчет при закрытии позиции.
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Currency closeCurrency = Currency.RUB;


    public static Symbol empty() {
        return Symbol.builder()
                .symbol("")
                .type(SymbolType.IRRELEVANT)
                .name("Новая позиция")
                .closeCurrency(Currency.RUB)
                .currency(Currency.RUB)
                .stockExchange(MCX)
                .build();
    }
}
