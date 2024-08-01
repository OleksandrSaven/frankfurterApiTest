package com.example.demo.service;

import com.example.demo.dto.FxResponse;
import com.example.demo.dto.FxResponseTimeSeries;

public interface CurrencyService {

    FxResponse getFxRates(String targetCurrency);

    FxResponseTimeSeries getFxRatesTimeSeries(String targetCurrency);

}
