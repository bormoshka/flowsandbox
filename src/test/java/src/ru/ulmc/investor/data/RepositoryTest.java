package src.ru.ulmc.investor.data;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ulmc.investor.Application;
import ru.ulmc.investor.data.entity.BasePosition;
import ru.ulmc.investor.data.entity.Currency;
import ru.ulmc.investor.data.entity.Portfolio;
import ru.ulmc.investor.data.repository.PortfolioRepository;
import ru.ulmc.investor.data.repository.PositionRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RepositoryTest {

    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private PositionRepository positionRepository;

    @Test
    public void testPortfolios() {

        Portfolio saved = portfolioRepository.save(getPortfolio());
        long id = saved.getId();
        Optional<Portfolio> fetched = portfolioRepository.findById(id);
        assertThat(fetched).isNotEmpty();
        log.info("Saved and fetched entity {}", fetched);

    }

    @Test
    @Transactional
    public void testBasePositions() {

        Portfolio portfolio = portfolioRepository.save(getPortfolio());
        long firstPortfolioId = portfolio.getId();
        positionRepository.save(getBasePosition(portfolio));
        positionRepository.save(getBasePosition(portfolio));
        positionRepository.save(getBasePosition(portfolio));
        positionRepository.save(getBasePosition(portfolio));

        portfolio = portfolioRepository.save(getPortfolio());
        positionRepository.save(getBasePosition(portfolio));
        positionRepository.save(getBasePosition(portfolio));

        ArrayList<BasePosition> list = new ArrayList<>(positionRepository.findAllByPortfolio_Id(firstPortfolioId));
        assertThat(list.size()).isEqualTo(4);
    }

    private BasePosition getBasePosition(Portfolio portfolio) {
        return BasePosition.builder()
                .account("MyAccount")
                .positionCurrency(Currency.RUB)
                .closeCurrency(Currency.RUB)
                .closed(false)
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
        portfolio.setPositions(new ArrayList<>());
        return portfolio;
    }
}
