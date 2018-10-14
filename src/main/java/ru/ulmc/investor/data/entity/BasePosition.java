package ru.ulmc.investor.data.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Entity
@Table(name = "BASE_POSITION")
@Builder
public class BasePosition {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "PORTFOLIO_ID", nullable = false)
    private Portfolio portfolio;

    /**
     * Наименование п.
     */
    @NonNull
    @Column(nullable = false)

    private String name;
    /**
     * Условное обозначние позиции
     */
    @NonNull
    @Column(nullable = false)
    private String code;

    @NonNull
    @Column(nullable = false)
    private Integer size;

    /**
     * Счет, к которому привязана позиция
     */
    private String account;

    /**
     * Валюта позиции
     */
    @NonNull
    @Column(nullable = false)
    private Currency positionCurrency;
    /**
     * Валюта, в которую будет осуществлен пересчет при закрытии позиции.
     */
    private Currency closeCurrency;
    /**
     * Дата открытия позиции
     */
    @NonNull
    @Column(nullable = false)
    private LocalDateTime openDate;

    /**
     * Цена открытия одной позиции в валюте позиции
     */
    @NonNull
    @Column(nullable = false)
    private BigDecimal openPrice;

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
     * Цена в переводе на
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean closed = false;
}
