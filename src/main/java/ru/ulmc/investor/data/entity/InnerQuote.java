package ru.ulmc.investor.data.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "CI_QUOTES")

@Getter
@Setter
@ToString
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class InnerQuote {
    @Id
    private UUID id;

    @NonNull
    @Column(nullable = false)
    private String symbol;

    @NonNull
    @Column(nullable = false)
    private LocalDate date;

    @NonNull
    @Column(nullable = false)
    private LocalTime openTime;

    @NonNull
    @Column(nullable = false)
    private LocalTime closeTime;

    @NonNull
    @Column(nullable = false)
    private BigDecimal openPrice;

    @NonNull
    @Column(nullable = false)
    private BigDecimal closePrice;

}
