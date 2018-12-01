package ru.ulmc.investor.data.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ulmc.investor.data.entity.Symbol;

import java.util.List;

@Repository
public interface StockRepository extends CrudRepository<Symbol, Long> {
    List<Symbol> findAllByBroker_Id(long id);

    Symbol saveAndFlush(Symbol symbol);

    @Query(value = "SELECT DISTINCT symbol FROM CI_SYMBOLS ", nativeQuery = true)
    List<String> selectAllSymbols();
}
