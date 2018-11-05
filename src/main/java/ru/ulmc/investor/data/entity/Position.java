package ru.ulmc.investor.data.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность базовой позиции.
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Entity
@Table(name = "CI_POSITION")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(toBuilder = true)
public class Position {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(nullable = true)
    private String comment;

    @NonNull
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "PORTFOLIO_ID", nullable = false)
    private Portfolio portfolio;

    @NonNull
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "STOCK_ID", nullable = false)
    private StockPosition stockPosition;

    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    /**
     * Дата открытия позиции
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime openDate = LocalDateTime.now();

    /**
     * Дата открытия позиции
     */
    @Builder.Default
    @Column(nullable = true)
    private LocalDateTime closeDate = LocalDateTime.now(); //todo: null

    /**
     * Цена открытия одной позиции в валюте позиции
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal openPrice = BigDecimal.ONE;

    /**
     * Цена валюты на момент отрытия
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal currencyOpenPrice = BigDecimal.ONE;


    /**
     * Цена закрытия одной позиции в валюте позиции
     */
    private BigDecimal closePrice;

    /**
     * Цена валюты на момент закрытия
     */
    private BigDecimal currencyClosePrice;

    /**
     * Признак закрытия позиции
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean closed = false;

    public Position copy() {
        return Position.builder()
                .stockPosition(stockPosition)
                .closed(closed)
                .quantity(quantity)
                .openDate(openDate)
                .openPrice(openPrice)
                .currencyOpenPrice(currencyOpenPrice)
                .portfolio(portfolio)
                .closePrice(closePrice)
                .closeDate(closeDate)
                .build();
    }
}
