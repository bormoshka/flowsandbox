package ru.ulmc.investor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ulmc.investor.data.repository.PortfolioRepository;
import ru.ulmc.investor.data.repository.PositionRepository;
import ru.ulmc.investor.ui.entity.PortfolioViewModel;
import ru.ulmc.investor.ui.entity.PositionViewModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository, PositionRepository positionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
    }

    public List<PortfolioViewModel> getAllPortfolios() {
        return StreamSupport.stream(portfolioRepository.findAll().spliterator(), false)
                .map(PortfolioViewModel::new)
                .collect(Collectors.toList());
    }

    public List<PositionViewModel> getAllOpenPositions(long portfolioId) {
        return positionRepository.findAllByClosedFalseAndPortfolio_Id(portfolioId).stream().map(PositionViewModel::new).collect(Collectors.toList());
    }
}
