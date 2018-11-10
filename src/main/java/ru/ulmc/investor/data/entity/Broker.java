package ru.ulmc.investor.data.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "CI_BROKER")
@Getter
@Setter
@Builder
@ToString(exclude = "instruments")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
public class Broker {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "broker", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Instrument> instruments;

    //todo: комиссии и т.п.


    Broker(String name) {
        this.name = name;
    }

    public static Broker empty() {
      return new Broker("Новый брокер");
    }
}
