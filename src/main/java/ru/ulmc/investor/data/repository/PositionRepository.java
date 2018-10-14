package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.ulmc.investor.data.entity.BasePosition;

import java.util.List;

@Repository
public interface PositionRepository extends CrudRepository<BasePosition, Long> {

    List<BasePosition> findAllByPortfolio_Id(long id);

    List<BasePosition> findAllByClosedTrueAndPortfolio_Id(long id);

    List<BasePosition> findAllByClosedFalseAndPortfolio_Id(long id);

}
