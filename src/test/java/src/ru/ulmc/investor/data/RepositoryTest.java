package src.ru.ulmc.investor.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import ru.ulmc.investor.Application;
import ru.ulmc.investor.data.entity.*;
import ru.ulmc.investor.data.entity.Symbol;
import ru.ulmc.investor.data.repository.PortfolioRepository;
import ru.ulmc.investor.data.repository.PositionRepository;
import ru.ulmc.investor.data.repository.StockRepository;
import ru.ulmc.investor.service.StocksService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(classes = Application.class)
public class RepositoryTest {

    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StocksService stocksService;

    @Test
    public void testPortfolios() {

        Portfolio saved = portfolioRepository.save(getPortfolio());
        long id = saved.getId();
        Optional<Portfolio> fetched = portfolioRepository.findById(id);
        assertThat(fetched).isNotEmpty();
        log.info("Saved and fetched entity {}", fetched);

    }

    @Test
    public void testBasePositions() {
        portfolioRepository.findAll().forEach(portfolioRepository::delete);
        Portfolio portfolio = stocksService.save(getPortfolio());
        Broker broker = stocksService.save(getBroker("Сбербанк"));
        Symbol symbol = stocksService.save(getStock(broker));
        long firstPortfolioId = portfolio.getId();
        stocksService.save(getBasePosition(portfolio, symbol));
        stocksService.save(getBasePosition(portfolio, symbol));
        stocksService.save(getBasePosition(portfolio, symbol));
        stocksService.save(getBasePosition(portfolio, symbol));

        portfolio = stocksService.save(getPortfolio());
        stocksService.save(getBasePosition(portfolio, symbol));
        stocksService.save(getBasePosition(portfolio, symbol));

        ArrayList<Position> list = new ArrayList<>(positionRepository.findAllByPortfolio_Id(firstPortfolioId));
        assertThat(list.size()).isEqualTo(4);
    }

    static Position getBasePosition(Portfolio portfolio, Symbol symbol) {
        return Position.builder()
                .symbol(symbol)
                .closed(false)
                .quantity(10)
                .openDate(LocalDateTime.now())
                .openPrice(BigDecimal.valueOf(10))
                .currencyOpenPrice(BigDecimal.ONE)
                .portfolio(portfolio)
                .build();
    }

    static Portfolio getPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setName("My portfolio " + UUID.randomUUID());
        portfolio.setPositions(new ArrayList<>());
        return portfolio;
    }

    static Broker getBroker(String name) {
        return Broker.builder().name(name).build();
    }

    static Symbol getStock(Broker broker, String name, String code) {
        return Symbol.builder()
                .name(name)
                .symbol(code)
                .type(SymbolType.STOCK)
                .stockExchange(StockExchange.NASDAQ)
                .currency(Currency.RUB)
                .closeCurrency(Currency.RUB)
                .broker(broker)
                .build();
    }

    static Symbol getStock(Broker broker) {
        return getStock(broker, "Google", "GOOG");
    }
}
