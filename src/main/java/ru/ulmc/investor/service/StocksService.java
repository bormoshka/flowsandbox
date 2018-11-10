package ru.ulmc.investor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulmc.investor.data.entity.Broker;
import ru.ulmc.investor.data.entity.Portfolio;
import ru.ulmc.investor.data.entity.Position;
import ru.ulmc.investor.data.entity.Instrument;
import ru.ulmc.investor.data.repository.BrokerRepository;
import ru.ulmc.investor.data.repository.PortfolioRepository;
import ru.ulmc.investor.data.repository.PositionRepository;
import ru.ulmc.investor.data.repository.StockRepository;
import ru.ulmc.investor.ui.entity.*;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional
public class StocksService {
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final BrokerRepository brokerRepository;
    private final StockRepository stockRepository;

    @Autowired
    public StocksService(PortfolioRepository portfolioRepository,
                         PositionRepository positionRepository,
                         BrokerRepository brokerRepository,
                         StockRepository stockRepository) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.brokerRepository = brokerRepository;
        this.stockRepository = stockRepository;
    }

    public List<InstrumentViewModel> getStockPositions() {
        Spliterator<Instrument> spliterator = stockRepository.findAll().spliterator();
        return StreamSupport.stream(spliterator, false)
                .map(InstrumentViewModel::of)
                .collect(toList());
    }

    public List<InstrumentViewModel> getStockPositionsByBrokerId(long brokerId) {
        return stockRepository.findAllByBroker_Id(brokerId).stream()
                .map(InstrumentViewModel::of)
                .collect(toList());
    }

    public List<BrokerLightModel> getBrokers() {
        Spliterator<Broker> spliterator = brokerRepository.findAll().spliterator();
        return StreamSupport.stream(spliterator, false)
                .map(BrokerLightModel::of)
                .collect(toList());
    }

    public Optional<Broker> getBroker(long id) {
        return brokerRepository.findById(id);
    }

    public List<PortfolioViewModel> getAllPortfolios() {
        Spliterator<Portfolio> spliterator = portfolioRepository.findAll().spliterator();
        List<PortfolioViewModel> portfolioList = StreamSupport.stream(spliterator, false)
                .map(PortfolioViewModel::of)
                .collect(toList());
        log.debug("Getting all portfolios, found {}", portfolioList.size());
        return portfolioList;
    }

    public List<PortfolioLightModel> getAllPortfoliosInfo() {
        Spliterator<Portfolio> spliterator = portfolioRepository.findAll().spliterator();
        List<PortfolioLightModel> portfolioList = StreamSupport.stream(spliterator, false)
                .map(PortfolioLightModel::of)
                .collect(toList());
        log.debug("Getting all portfolios, found {}", portfolioList.size());
        return portfolioList;
    }

    public List<PositionViewModel> getAllOpenPositions(long portfolioId) {
        return toPositionViewModel(positionRepository.findAllByClosedFalseAndPortfolio_Id(portfolioId));
    }

    public List<PositionViewModel> getClosedPositions(long portfolioId) {
        return toPositionViewModel(positionRepository.findAllByClosedTrueAndPortfolio_Id(portfolioId));
    }

    public List<PositionViewModel> getAllPositions(long portfolioId) {
        return toPositionViewModel(positionRepository.findAllByPortfolio_Id(portfolioId));
    }

    public void removePortfolio(long portfolioId) {
        portfolioRepository.deleteById(portfolioId);
        log.debug("Removed portfolio with id {}", portfolioId);
    }

    public void removeBroker(long id) {
        brokerRepository.deleteById(id);
        log.debug("Removed broker with id {}", id);
    }

    public void removeStock(long id) {
        stockRepository.deleteById(id);
        log.debug("Removed stock position with id {}", id);
    }

    public Portfolio save(Portfolio model) {
        log.debug("Trying to save portfolio from model {}", model);
        return portfolioRepository.save(model);
    }

    public void closeFractionally(Position open, Position closed) {
        log.debug("Trying to close position from open {} to closed {}", open, closed);
        Optional<Portfolio> portfolio = portfolioRepository.findById(open.getPortfolio().getId());
        Optional<Instrument> stock = stockRepository.findById(open.getInstrument().getId());

        if (portfolio.isPresent() && stock.isPresent()) {
            open.setPortfolio(portfolio.get());
            open.setInstrument(stock.get());
            closed.setPortfolio(portfolio.get());
            closed.setInstrument(stock.get());
            open.setQuantity(open.getQuantity() - closed.getQuantity());
            positionRepository.save(open);
            positionRepository.save(closed);
        }
    }

    public Position save(Position model) {
        log.debug("Trying to save position from model {}", model);
        Optional<Portfolio> portfolio = portfolioRepository.findById(model.getPortfolio().getId());
        Optional<Instrument> stock = stockRepository.findById(model.getInstrument().getId());
        if (portfolio.isPresent() && stock.isPresent()) {
            model.setPortfolio(portfolio.get());
            model.setInstrument(stock.get());
            return positionRepository.save(model);
        }
        return null;
    }

    public Instrument save(Instrument model) {
        log.debug("Trying to save position from model {}", model);
        Optional<Broker> byId = brokerRepository.findById(model.getBroker().getId());
        if (byId.isPresent()) {
            model.setBroker(byId.get());
            return stockRepository.save(model);
        }
        return null;
    }

    public Broker save(Broker model) {
        log.debug("Trying to save position from model {}", model);
        return brokerRepository.save(model);
    }

    private List<PositionViewModel> toPositionViewModel(List<Position> models) {
        return models.stream().map(PositionViewModel::of).collect(toList());
    }
}
