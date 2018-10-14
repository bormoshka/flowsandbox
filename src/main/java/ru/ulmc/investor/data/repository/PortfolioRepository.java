package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ulmc.investor.data.entity.Portfolio;

@Repository
public interface PortfolioRepository extends CrudRepository<Portfolio, Long> {


}
