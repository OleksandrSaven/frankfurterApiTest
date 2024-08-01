package com.example.demo.controller;

import com.example.demo.dto.FxResponse;
import com.example.demo.dto.FxResponseTimeSeries;
import com.example.demo.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/fx")
@RequiredArgsConstructor
public class CurrencyConversionController {
    private final CurrencyService currencyService;

    @GetMapping
    public FxResponse fxRates(@RequestParam(required = false,
            defaultValue = "EUR") String targetCurrency) {
        return  currencyService.getFxRates(targetCurrency);
    }

    @GetMapping("/{targetCurrency}")
    public FxResponseTimeSeries fxRateByTimeSeries(@PathVariable String targetCurrency) {
        return currencyService.getFxRatesTimeSeries(targetCurrency);
    }
}
