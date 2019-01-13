package ru.ulmc.investor.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulmc.investor.data.entity.*;
import ru.ulmc.investor.data.repository.*;
import ru.ulmc.investor.ui.entity.*;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Slf4j
@Service
@Transactional
public class StocksService {
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final BrokerRepository brokerRepository;
    private final StockRepository stockRepository;
    private final LastPriceRepository lastPriceRepository;

    @Autowired
    public StocksService(PortfolioRepository portfolioRepository,
                         PositionRepository positionRepository,
                         BrokerRepository brokerRepository,
                         StockRepository stockRepository,
                         LastPriceRepository lastPriceRepository) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.brokerRepository = brokerRepository;
        this.stockRepository = stockRepository;
        this.lastPriceRepository = lastPriceRepository;
    }

    public List<SymbolViewModel> getStockPositions() {
        Spliterator<Symbol> spliterator = stockRepository.findAll().spliterator();
        return stream(spliterator, false)
                .map(SymbolViewModel::of)
                .collect(toList());
    }

    public List<SymbolViewModel> getStockPositionsByBrokerId(long brokerId) {
        return stockRepository.findAllByBroker_Id(brokerId).stream()
                .map(SymbolViewModel::of)
                .collect(toList());
    }

    public List<BrokerLightModel> getBrokers() {
        Spliterator<Broker> spliterator = brokerRepository.findAll().spliterator();
        return stream(spliterator, false)
                .map(BrokerLightModel::of)
                .collect(toList());
    }

    public Optional<Broker> getBroker(long id) {
        return brokerRepository.findById(id);
    }

    public List<PortfolioViewModel> getAllPortfolios() {
        return getPortfolio(PortfolioViewModel::of);
    }

    public List<PortfolioLightModel> getAllPortfoliosInfo() {
        return getPortfolio(PortfolioLightModel::of);
    }

    private <T> List<T> getPortfolio(Function<Portfolio, T> mapper) {
        Spliterator<Portfolio> spliterator = portfolioRepository.findAll().spliterator();
        List<T> portfolioList = stream(spliterator, false)
                .map(mapper)
                .collect(toList());
        log.debug("Getting all portfolios, found {}", portfolioList.size());
        return portfolioList;
    }

    public List<PositionViewModel> getAllOpenPositions(long portfolioId) {
        return toPositionViewModelAlt(positionRepository.findAllByClosedFalseAndPortfolio_Id(portfolioId));
    }

    public List<PositionViewModel> getClosedPositions(long portfolioId) {
        return toPositionViewModelAlt(positionRepository.findAllByClosedTrueAndPortfolio_Id(portfolioId));
    }

    public List<PositionViewModel> getAllPositions(long portfolioId) {
        return toPositionViewModelAlt(positionRepository.findAllByPortfolio_Id(portfolioId));
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
        Optional<Symbol> stock = stockRepository.findById(open.getSymbol().getId());

        if (portfolio.isPresent() && stock.isPresent()) {
            open.setPortfolio(portfolio.get());
            open.setSymbol(stock.get());
            closed.setPortfolio(portfolio.get());
            closed.setSymbol(stock.get());
            open.setQuantity(open.getQuantity() - closed.getQuantity());
            positionRepository.save(open);
            positionRepository.save(closed);
        }
    }

    @Transactional
    public void closeAll(Position closedPositionStats) {
        log.debug("Trying to close all positions {} by symbol", closedPositionStats);
        Long portfolioId = closedPositionStats.getPortfolio().getId();
        Symbol symbol = closedPositionStats.getSymbol();
        List<Position> all = positionRepository.findAllByPortfolio_IdAndSymbolAndClosedFalse(portfolioId, symbol);
        all.forEach(position -> {
            position.setClosed(true);
            position.setCloseDate(closedPositionStats.getCloseDate());
            position.setClosePrice(closedPositionStats.getClosePrice());
            position.setCurrencyClosePrice(closedPositionStats.getCurrencyClosePrice());
        });
        positionRepository.saveAll(all);
    }

    public Position save(Position model) {
        log.debug("Trying to save position from model {}", model);
        Optional<Portfolio> portfolio = portfolioRepository.findById(model.getPortfolio().getId());
        Optional<Symbol> stock = stockRepository.findById(model.getSymbol().getId());
        if (portfolio.isPresent() && stock.isPresent()) {
            model.setPortfolio(portfolio.get());
            model.setSymbol(stock.get());
            return positionRepository.save(model);
        }
        return null;
    }

    public Symbol save(Symbol model) {
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

    public Optional<LastPrice> findLastPrice(String stockCode) {
        return ofNullable(lastPriceRepository.findFirstBySymbolOrderByDateTimeDesc(stockCode));
    }

    private List<PositionViewModel> toPositionViewModel(
            List<Position> models) { // todo: не понял пока какой способ лучше. Удалить лишний
        val data = models.stream().map(PositionViewModel::of).collect(toList());
        data.forEach(this::setupLastPriceFromCache);
        return data;
    }

    private void setupLastPriceFromCache(PositionViewModel model) {
        Optional<LastPrice> optionalPrice = findLastPrice(model.getStockCode());
        optionalPrice.ifPresent(lastPrice -> model.setMarketPrice(lastPrice.getLastPrice()));
    }

    private List<PositionViewModel> toPositionViewModelAlt(List<Position> models) {
        return models.stream()
                .map(position -> Pair.of(position, findLastPrice(position.getSymbol().getSymbol())))
                .map(PositionViewModel::of)
                .collect(toList());
    }

}
