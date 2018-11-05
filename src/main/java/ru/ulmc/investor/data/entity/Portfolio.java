package ru.ulmc.investor.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"positions"})
@Entity
@Table(name = "CI_PORTFOLIO")
@EqualsAndHashCode(of = "id")
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "NAME", unique = true)
    private String name;

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
    private List<Position> positions;

    public Portfolio() {
        positions = Collections.emptyList();
    }

    public Portfolio(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Portfolio(String name) {
        this.name = name;
    }
}
