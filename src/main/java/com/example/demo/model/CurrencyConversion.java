package com.example.demo.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "currency_conversions")
@Getter
@Setter
public class CurrencyConversion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long amount;
    private String base;
    private LocalDate date;
    @Column(name = "rate")
    @ElementCollection
    @CollectionTable(
            name = "exchange_rates",
            joinColumns = @JoinColumn(name = "currency_conversions_id"))
    @MapKeyColumn(name = "currency")
    private Map<String, BigDecimal> rates;
}
