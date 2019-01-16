package ru.ulmc.investor.data.entity;

import org.atmosphere.config.service.Get;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "CI_CACHE_HISTORY_PRICES",
        uniqueConstraints = @UniqueConstraint(name = "CI_UC_HIST_SYMBOL_DATE",
                columnNames = {"symbol", "date"}))

@Getter
@Setter
@ToString
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class HistoryPrice {
    @EmbeddedId
    private HistoryPriceId id;
    private BigDecimal volume;
    private BigDecimal close;

    public LocalDate getDate() {
        return id.getDate();
    }

    public String getSymbol() {
        return id.getSymbol();
    }

    @Getter
    @Setter
    @Embeddable
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @Builder
    public static class HistoryPriceId {

        @Column(name = "SYMBOL")
        private String symbol;
        @Column(name = "DATE")
        private LocalDate date;
    }
}
