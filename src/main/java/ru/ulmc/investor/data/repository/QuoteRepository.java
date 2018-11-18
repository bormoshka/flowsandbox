package ru.ulmc.investor.data.repository;

import org.springframework.data.repository.CrudRepository;
import ru.ulmc.investor.data.entity.InnerQuote;

import java.util.UUID;

public interface QuoteRepository extends CrudRepository<InnerQuote, UUID> {
}
