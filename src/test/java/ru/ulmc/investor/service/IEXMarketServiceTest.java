package ru.ulmc.investor.service;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;
import pl.zankowski.iextrading4j.api.stocks.Company;
import ru.ulmc.investor.Application;
import ru.ulmc.investor.data.entity.InnerQuote;
import ru.ulmc.investor.service.ExternalMarketService;
import ru.ulmc.investor.service.IEXMarketService;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.*;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("external-sources")
@SpringBootTest(classes = Application.class)
public class IEXMarketServiceTest {

    public static final String AAPL = "AAPL";
    public static final String GOOG = "GOOG";
    public static final String AMD = "AMD";

    @Autowired
    private ExternalMarketService service;

    @Test
    public void getQuoteAsync() {
        StopWatch stopWatch = new StopWatch("async");
        stopWatch.start("future");
        // этот вариант мне не очень нравится.
        try {
            InnerQuote aapl = service.getQuoteAsync("AAPL").get();
            log.info("Fetched {}", aapl);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            fail();
            return;
        } finally {
            stopWatch.stop();
        }

        stopWatch.start("consumer");
        service.getQuoteAsync(AAPL, innerQuote -> {
            log.info("Fetched {}", innerQuote);
            stopWatch.stop();
            log.info(stopWatch.prettyPrint());
            assertEquals(innerQuote.getSymbol(), AAPL);
        });

    }

    @Test
    public void getSymbols() {
        Optional<Company> aapl = service.getCompany(AAPL);
        assertEquals(aapl.get().getSymbol(), AAPL);

        Optional<Company> acme = service.getCompany("NOT_AN_ACME");
        assertFalse(acme.isPresent());
    }
}
