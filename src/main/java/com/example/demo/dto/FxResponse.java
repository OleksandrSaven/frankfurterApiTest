package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class FxResponse {
    private LocalDate date;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal exchangeRate;
}
