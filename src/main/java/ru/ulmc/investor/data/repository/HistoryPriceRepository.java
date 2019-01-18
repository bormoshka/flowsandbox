package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ru.ulmc.investor.data.entity.Broker;
import ru.ulmc.investor.data.entity.HistoryPrice;
import ru.ulmc.investor.data.entity.HistoryPrice.HistoryPriceId;

@Repository
public interface HistoryPriceRepository extends CrudRepository<HistoryPrice, HistoryPriceId> {

}
