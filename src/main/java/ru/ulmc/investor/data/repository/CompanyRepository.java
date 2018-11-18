package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import ru.ulmc.investor.data.entity.CompanyInfo;

import java.util.List;

public interface CompanyRepository extends CrudRepository<CompanyInfo, Long> {

    CompanyInfo findFirstBySymbol(String symbol);

    List<CompanyInfo> findTop30BySymbolContainingOrderBySymbol(String substring);
}
