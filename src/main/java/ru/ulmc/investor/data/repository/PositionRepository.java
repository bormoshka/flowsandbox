package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ulmc.investor.data.entity.Position;
import ru.ulmc.investor.data.entity.Symbol;

import java.util.List;

@Repository
public interface PositionRepository extends CrudRepository<Position, Long> {

    List<Position> findAllByPortfolio_Id(long id);

    List<Position> findAllByPortfolio_IdAndSymbolAndClosedFalse(long id, Symbol symbol);

    List<Position> findAllByClosedTrueAndPortfolio_Id(long id);

    List<Position> findAllByClosedFalseAndPortfolio_Id(long id);

}
