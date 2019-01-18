package ru.ulmc.investor.ui.view.information;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.ui.entity.position.PositionResultViewModel;
import ru.ulmc.investor.ui.entity.position.PositionResultViewModel.PerCurrencyResult;
import ru.ulmc.investor.ui.entity.position.PositionViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

@Slf4j
@Tag("position-sum")
@HtmlImport("frontend://src/position/position-sum.html")
public class PositionSumComponent extends PolymerTemplate<PositionResultViewModel> {
    private static final String EMPTY_NAME_GREETING = "Please enter your name";

    public PositionSumComponent() {
        //setId("template");
    }

    public void update(List<PositionViewModel> viewModels) {
        Map<String, PerCurrencyResult> collect = viewModels.stream()
                .map(this::getPerCurrencyResult)
                .collect(groupingBy(PerCurrencyResult::getCurrency, reducing(new PerCurrencyResult(), this::getReduceStrategy)));

        getModel().setBaseCurrency(Currency.RUB.name());
        getModel().setPerCurrencyResults(new ArrayList<>(collect.values()));
    }

    private PerCurrencyResult getReduceStrategy(PerCurrencyResult o,
                                                PerCurrencyResult o2) {
        return o.toBuilder()
                .totalProfit(o.getTotalProfit().add(o2.getTotalProfit()))
                .totalInvested(o.getTotalInvested().add(o2.getTotalInvested()))
                .currency(o2.getCurrency())
                .hasAnyClosedPositions(o.isHasAnyClosedPositions() || o2.isHasAnyClosedPositions())
                .build()
                .recalc();
    }

    private PerCurrencyResult getPerCurrencyResult(PositionViewModel pvm) {
        return PerCurrencyResult.builder()
                .currency(pvm.getBaseCurrency())
                .totalProfit(pvm.getTotals().getProfit())
                .totalInvested(pvm.getInvestedSummary())
                .hasAnyClosedPositions(pvm.isClosed())
                .build();
    }

}
