package com.example.demo.service.impl;

import com.example.demo.dto.FxResponse;
import com.example.demo.dto.FxResponseTimeSeries;
import com.example.demo.model.CurrencyConversion;
import com.example.demo.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class CurrencyServiceImplTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFxRatesFromDb() {
        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setBase("USD");
        conversion.setDate(LocalDate.now());
        conversion.setRates(Map.of("EUR", new BigDecimal("0.85")));

        when(currencyRepository.findByDateAndBase(any(LocalDate.class), eq("EUR")))
                .thenReturn(Optional.of(conversion));

        FxResponse response = currencyService.getFxRates("EUR");

        assertNotNull(response);
        assertEquals("USD", response.getSourceCurrency());
        assertEquals("EUR", response.getTargetCurrency());
        assertEquals(new BigDecimal("0.85"), response.getExchangeRate());
    }

    @Test
    void testGetFxRatesFromApi() {
        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setBase("USD");
        conversion.setDate(LocalDate.now());
        conversion.setRates(Map.of("EUR", new BigDecimal("0.85")));

        when(currencyRepository.findByDateAndBase(any(LocalDate.class), eq("EUR")))
                .thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(CurrencyConversion.class)))
                .thenReturn(conversion);

        FxResponse response = currencyService.getFxRates("EUR");

        assertNotNull(response);
        assertEquals("USD", response.getSourceCurrency());
        assertEquals("EUR", response.getTargetCurrency());
        assertEquals(new BigDecimal("0.92353"), response.getExchangeRate());
        verify(currencyRepository, times(1)).save(any(CurrencyConversion.class));
    }

    @Test
    void testGetFxRatesTimeSeriesFromApi() {
        FxResponseTimeSeries fxResponseTimeSeries = new FxResponseTimeSeries();
        fxResponseTimeSeries.setBase("USD");
        fxResponseTimeSeries.setRates(Map.of(
                "2024-07-29", Map.of("CZK", new BigDecimal("23.45500")),
                "2024-07-30", Map.of("CZK", new BigDecimal("23.49300")),
                "2024-07-26", Map.of("CZK", new BigDecimal("23.36100"))
        ));

        when(currencyRepository.findByDateBetweenAndBase(any(LocalDate.class), any(LocalDate.class), eq("USD")))
                .thenReturn(List.of());
        when(restTemplate.getForObject(anyString(), eq(FxResponseTimeSeries.class)))
                .thenReturn(fxResponseTimeSeries);

        FxResponseTimeSeries response = currencyService.getFxRatesTimeSeries("CZK");

        assertNotNull(response);
        assertEquals("USD", response.getBase());
        verify(currencyRepository, times(3)).save(any(CurrencyConversion.class));
    }
}