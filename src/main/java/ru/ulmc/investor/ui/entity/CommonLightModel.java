package ru.ulmc.investor.ui.entity;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class CommonLightModel implements Serializable {
    long id;
    String name;


}
