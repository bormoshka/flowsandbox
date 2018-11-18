package ru.ulmc.investor.data.entity;


import com.vaadin.flow.templatemodel.TemplateModel;
import lombok.*;

import javax.persistence.*;

import static ru.ulmc.investor.data.entity.StockExchange.UNKNOWN;

@Entity
@Table(name = "CI_CACHE_COMPANIES",
        uniqueConstraints = @UniqueConstraint(name = "CI_UC_COMP_SYMBOL", columnNames = {"symbol"}))

@Getter
@Setter
@ToString
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Наименование компании.
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


    @NonNull
    @Column(nullable = false)
    private String description;

    @NonNull
    @Column(nullable = false)
    private String industry;

    @NonNull
    @Column(nullable = false)
    private String sector;

    @NonNull
    @Column(nullable = false)
    private String ceo;

    @NonNull
    @Column(nullable = false)
    private SymbolType type;

    /**
     * Код биржи.
     */
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private StockExchange stockExchange = UNKNOWN;
}
