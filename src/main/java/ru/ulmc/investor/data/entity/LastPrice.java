package ru.ulmc.investor.data.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CI_CACHE_LAST_PRICES",
        uniqueConstraints = @UniqueConstraint(name = "CI_UC_LAST_PRICE_SYMBOL_AND_TIME",
                columnNames = {"symbol", "dateTime"}))
@Getter
@Setter
@ToString
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class LastPrice {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @NonNull
    @Column(nullable = false)
    private String symbol;

    @NonNull
    @Column(nullable = false)
    private LocalDateTime dateTime;

    @NonNull
    @Column(nullable = false)
    private BigDecimal lastPrice;

    @NonNull
    @Column(nullable = false)
    private BigDecimal volume;

}
