package com.example.demo.repository;

import com.example.demo.model.CurrencyConversion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyConversion, Long> {

    Optional<CurrencyConversion> findByDateAndBase(LocalDate date, String base);

    List<Optional<CurrencyConversion>> findByDateBetweenAndBase(LocalDate fromDate,
                                                                LocalDate toDate, String base);

}
