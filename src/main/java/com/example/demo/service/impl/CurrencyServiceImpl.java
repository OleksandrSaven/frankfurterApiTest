package com.example.demo.service.impl;

import com.example.demo.dto.FxResponse;
import com.example.demo.dto.FxResponseTimeSeries;
import com.example.demo.model.CurrencyConversion;
import com.example.demo.repository.CurrencyRepository;
import com.example.demo.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {
    private static final String BASE_URL = "https://api.frankfurter.app/";
    private static final String SPECIFICATION = "?from=USD&to=";
    private static final String CURRENCIES = "EUR,GBP,JPY,CZK";
    private static final String LATEST = "latest";
    private static final String BASE = "USD";
    private static final int DAYS_TO_SUBSTRACT = 5;
    private static final int NUMBER_DAYS = 3;
    private static final int AMOUNT = 1;
    private final CurrencyRepository currencyRepository;

    @Override
    public FxResponse getFxRates(String targetCurrency) {
        if (getFromDb(targetCurrency).isPresent()) {
            CurrencyConversion currencyConversion = getFromDb(targetCurrency).get();
            return mapToFxResponse(currencyConversion, targetCurrency);
        } else {
            return fetchAndSaveFxRate(targetCurrency);
        }
    }

    @Override
    public FxResponseTimeSeries getFxRatesTimeSeries(String targetCurrency) {
        if (getFromDbTimeSeries(targetCurrency).isPresent()) {
            return getFromDbTimeSeries(targetCurrency).get();
        } else {
            return fetchAndSaveTimeSeries(targetCurrency);
        }
    }

    private FxResponseTimeSeries fetchAndSaveTimeSeries(String targetCurrency) {
        LocalDate today = LocalDate.now();
        LocalDate before = today.minusDays(DAYS_TO_SUBSTRACT);

        RestTemplate restTemplate = new RestTemplate();
        FxResponseTimeSeries responce = restTemplate.getForObject(
                BASE_URL + before.toString() + ".." + SPECIFICATION + targetCurrency,
                FxResponseTimeSeries.class);
        Set<String> dateSet = responce.getRates().keySet();

        dateSet.stream().limit(NUMBER_DAYS).forEach(date -> {
            Optional<CurrencyConversion> existRecord =
                    currencyRepository.findByDateAndBase(LocalDate.parse(date), BASE);
            if (existRecord.isEmpty()) {
                CurrencyConversion currencyConversion = new CurrencyConversion();
                currencyConversion.setBase(responce.getBase());
                currencyConversion.setAmount(AMOUNT);
                currencyConversion.setDate(LocalDate.parse(date));
                currencyConversion.setRates(responce.getRates().get(date));
                currencyRepository.save(currencyConversion);
            }
        });
        return responce;
    }

    private Optional<CurrencyConversion> getFromDb(String targetCurrency) {
        LocalDate today = LocalDate.now();
        return currencyRepository.findByDateAndBase(today, targetCurrency);
    }

    private Optional<FxResponseTimeSeries> getFromDbTimeSeries(String targetCurrency) {
        LocalDate today = LocalDate.now();
        List<Optional<CurrencyConversion>> byDateBetweenAndBase =
                currencyRepository.findByDateBetweenAndBase(today.minusDays(DAYS_TO_SUBSTRACT), today, BASE);
        if (byDateBetweenAndBase.size() > 2) {
            FxResponseTimeSeries responce = new FxResponseTimeSeries();
            responce.setBase(BASE);
            responce.setRates(convertToMap(byDateBetweenAndBase, targetCurrency));
            return Optional.of(responce);
        } else {
            return Optional.empty();
        }
    }

    private Map<String, Map<String, BigDecimal>> convertToMap(
            List<Optional<CurrencyConversion>> byDateBetweenAndBase, String targetCurrency) {
        Map<String, Map<String, BigDecimal>> rates = new HashMap<>();

        for (Optional<CurrencyConversion> optionalCurrencyConversion : byDateBetweenAndBase) {
            if (optionalCurrencyConversion.isPresent()) {
                CurrencyConversion conversion = optionalCurrencyConversion.get();
                String date = conversion.getDate().toString();
                String currency = conversion.getBase();
                Map<String, BigDecimal> rate = new HashMap<>();
                BigDecimal value = conversion.getRates().get(targetCurrency);
                if (value != null) {
                    rate.put(targetCurrency, value);
                }
                rates.computeIfAbsent(date, k -> rate);
            }
        }
        return rates;
    }

    private FxResponse fetchAndSaveFxRate(String targetCurrency) {
        RestTemplate restTemplate = new RestTemplate();
        CurrencyConversion fxResponse = restTemplate.getForObject(BASE_URL
                + LATEST + SPECIFICATION + CURRENCIES, CurrencyConversion.class);
        Optional<CurrencyConversion> optionalCurrencyConversion =
                currencyRepository.findByDateAndBase(fxResponse.getDate(), fxResponse.getBase());
        if (optionalCurrencyConversion.isPresent()) {
            return mapToFxResponse(optionalCurrencyConversion.get(), targetCurrency);
        } else {
            CurrencyConversion currencyConversion = new CurrencyConversion();
            currencyConversion.setDate(fxResponse.getDate());
            currencyConversion.setBase(fxResponse.getBase());
            currencyConversion.setRates(fxResponse.getRates());
            currencyConversion.setAmount(fxResponse.getAmount());
            try {
                currencyRepository.save(currencyConversion);
            } catch (DataIntegrityViolationException exception) {
                throw new RuntimeException("Data already exist in database");
            }
            return mapToFxResponse(currencyConversion, targetCurrency);
        }
    }

    private FxResponse mapToFxResponse(CurrencyConversion currencyConversion, String targetCurrency) {
        FxResponse response = new FxResponse();
        response.setDate(currencyConversion.getDate());
        response.setSourceCurrency(currencyConversion.getBase());
        response.setTargetCurrency(targetCurrency);
        response.setExchangeRate(currencyConversion.getRates().get(targetCurrency));
        return response;
    }
}
