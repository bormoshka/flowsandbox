package ru.ulmc.investor.ui.entity;

import lombok.*;
import lombok.experimental.Delegate;
import ru.ulmc.investor.data.entity.BasePosition;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class PositionViewModel {

    @Delegate
    private final BasePosition basePosition;

}
