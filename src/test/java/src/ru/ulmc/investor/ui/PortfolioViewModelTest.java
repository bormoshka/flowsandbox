package src.ru.ulmc.investor.ui;

import org.junit.Test;
import ru.ulmc.investor.data.entity.*;
import ru.ulmc.investor.ui.entity.PortfolioViewModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PortfolioViewModelTest {
    @Test
    public void test() {
        PortfolioViewModel pmv = PortfolioViewModel.of(getPortfolio());
        assertThat(pmv.getPerCurrencyPositionStats().size()).isEqualTo(3);
        PortfolioViewModel.PositionsStat eur = pmv.getPerCurrencyPositionStats().get(Currency.EUR);
        assertThat(eur.getProfit()).isEqualTo(BigDecimal.ZERO);
        assertThat(eur.getClosedPositionCount()).isEqualTo(0);
        assertThat(eur.getClosedPositionSum()).isEqualTo(BigDecimal.ZERO);
        assertThat(eur.getOpenPositionCount()).isEqualTo(3);
        assertThat(eur.getOpenPositionSum()).isEqualTo(BigDecimal.valueOf(300));
    }

    private Position getBasePosition(Portfolio portfolio, Currency currency, boolean isClosed) {
        Symbol symbol = Symbol.builder()
                .name("Google " + UUID.randomUUID().toString())
                .symbol("GOOG")
                .type(SymbolType.STOCK)
                .stockExchange(StockExchange.NASDAQ)
                .broker(Broker.builder().name("Sberbank").build())
                .currency(currency)
                .closeCurrency(currency)
                .build();
        return Position.builder()
                .symbol(symbol)
                .closed(isClosed)
                .currencyClosePrice(BigDecimal.ONE)
                .closePrice(BigDecimal.valueOf(3))
                .quantity(10)
                .openDate(LocalDateTime.now())
                .openPrice(BigDecimal.valueOf(10))
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
