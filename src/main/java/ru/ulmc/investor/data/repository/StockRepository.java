package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ulmc.investor.data.entity.Instrument;

import java.util.List;

@Repository
public interface StockRepository extends CrudRepository<Instrument, Long> {
    List<Instrument> findAllByBroker_Id(long id);

    Instrument saveAndFlush(Instrument instrument);

}
