package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ulmc.investor.data.entity.StockPosition;

import java.util.List;

@Repository
public interface StockRepository extends CrudRepository<StockPosition, Long> {
    List<StockPosition> findAllByBroker_Id(long id);

    StockPosition saveAndFlush(StockPosition stockPosition);

}
