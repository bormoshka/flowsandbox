package src.ru.ulmc.investor.ui;

import org.junit.Test;
import ru.ulmc.investor.data.entity.BasePosition;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.data.entity.Portfolio;
import ru.ulmc.investor.ui.entity.PortfolioViewModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PortfolioViewModelTest {
    @Test
    public void test() {
        PortfolioViewModel pmv = new PortfolioViewModel(getPortfolio());
        assertThat(pmv.getPerCurrencyPositionStats().size()).isEqualTo(3);
        PortfolioViewModel.PositionsStat eur = pmv.getPerCurrencyPositionStats().get(Currency.EUR);
        assertThat(eur.getProfit()).isEqualTo(BigDecimal.ZERO);
        assertThat(eur.getClosedPositionCount()).isEqualTo(0);
        assertThat(eur.getClosedPositionSum()).isEqualTo(BigDecimal.ZERO);
        assertThat(eur.getOpenPositionCount()).isEqualTo(3);
        assertThat(eur.getOpenPositionSum()).isEqualTo(BigDecimal.valueOf(300));
    }

    private BasePosition getBasePosition(Portfolio portfolio, Currency currency, boolean isClosed) {
        return BasePosition.builder()
                .account("MyAccount")
                .positionCurrency(currency)
                .closeCurrency(currency)
                .closed(isClosed)
                .currencyClosePrice(BigDecimal.ONE)
                .closePrice(BigDecimal.valueOf(3))
                .size(10)
                .openDate(LocalDateTime.now())
                .openPrice(BigDecimal.valueOf(10))
                .code("GOOG")
                .name("Google " + UUID.randomUUID().toString())
                .currencyOpenPrice(BigDecimal.ONE)
                .portfolio(portfolio)
                .build();
    }

    private Portfolio getPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setName("My portfolio");
        portfolio.setPositions(Arrays.asList(
                getBasePosition(portfolio, Currency.EUR, false),
                getBasePosition(portfolio, Currency.EUR, false),
                getBasePosition(portfolio, Currency.EUR, false),
                getBasePosition(portfolio, Currency.RUB, false),
                getBasePosition(portfolio, Currency.RUB, false),
                getBasePosition(portfolio, Currency.RUB, true),
                getBasePosition(portfolio, Currency.RUB, true),
                getBasePosition(portfolio, Currency.USD, true),
                getBasePosition(portfolio, Currency.USD, true),
                getBasePosition(portfolio, Currency.USD, true),
                getBasePosition(portfolio, Currency.USD, true)
        ));
        return portfolio;
    }
}
