package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FxResponse {
    private LocalDate date;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal exchangeRate;
}
