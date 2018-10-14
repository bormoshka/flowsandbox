package ru.ulmc.investor.data.entity;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class MonitoringEvent implements Serializable {
    private String id;
    private String name;
    private LocalDateTime timeAndDate;
    private String description;
    private String user;
}
