package ru.ulmc.investor.data;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.ulmc.investor.Application;
import ru.ulmc.investor.data.entity.Broker;
import ru.ulmc.investor.data.repository.BrokerRepository;
import ru.ulmc.investor.data.repository.StockRepository;
import ru.ulmc.investor.service.StocksService;

import static ru.ulmc.investor.data.RepositoryTest.getStock;

@Slf4j
@RunWith(SpringRunner.class)
@Transactional
@ActiveProfiles("random-sources")
@SpringBootTest(classes = Application.class)
public class DemoDataFillTest {

    @Autowired
    private BrokerRepository brokerRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private StocksService stocksService;

    @Test
    @Rollback(false)
    public void test() {
        stockRepository.findAll().forEach(stock -> stockRepository.delete(stock));
        Iterable<Broker> brokers = brokerRepository.findAll();
        brokers.forEach(broker -> brokerRepository.delete(broker));
        Broker broker = stocksService.save(RepositoryTest.getBroker("Сбербанк"));
        Broker broker2 = stocksService.save(RepositoryTest.getBroker("Тинькофф"));

        stocksService.save(RepositoryTest.getStock(broker2, "Alphabet Inc Class C", "GOOG"));
        stocksService.save(RepositoryTest.getStock(broker2, "Alphabet Inc Class A", "GOOGL"));
        stocksService.save(RepositoryTest.getStock(broker2, "Netflix, Inc.", "NTLX"));
        stocksService.save(RepositoryTest.getStock(broker2, "Advanced Micro Devices", "AMD"));
        stocksService.save(RepositoryTest.getStock(broker2, "Tesla Inc", "TSLA"));

        stocksService.save(RepositoryTest.getStock(broker, "Сбербанк России", "SBER"));
        stocksService.save(RepositoryTest.getStock(broker, "Яндекс, NV", "YNDX"));
        stocksService.save(RepositoryTest.getStock(broker, "Gazprom PAO", "GAZP"));
        stocksService.save(RepositoryTest.getStock(broker, "Novolipetsk Steel PAO", "NLMK"));

        stocksService.getStockPositions().forEach(stockPosition -> {
            log.info("Found {}", stockPosition);
        });
    }
}
