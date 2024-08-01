package com.example.demo.controller;

import com.example.demo.dto.FxResponse;
import com.example.demo.dto.FxResponseTimeSeries;
import com.example.demo.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyConversionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CurrencyService currencyService;

    @Test
    void testFxRates_Ok() throws Exception {
        FxResponse mockResponse = new FxResponse();
        mockResponse.setDate(LocalDate.parse("2024-07-30"));
        mockResponse.setSourceCurrency("USD");
        mockResponse.setTargetCurrency("EUR");
        mockResponse.setExchangeRate(BigDecimal.valueOf(0.92387));
        when(currencyService.getFxRates("EUR")).thenReturn(mockResponse);
        mockMvc.perform(get("/fx")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{ 'date' : '2024-07-30', 'sourceCurrency': 'USD', " +
                        "'targetCurrency': 'EUR', 'exchangeRate': 0.92387 }"));
    }

    @Test
    void testFxRateByTimeSeries_Ok() throws Exception {
        FxResponseTimeSeries mockResponse = new FxResponseTimeSeries();
        mockResponse.setBase("USD");
        mockResponse.setRates(Map.of(
                "2024-07-29", Map.of("CZK", new BigDecimal("23.45500")),
                "2024-07-30", Map.of("CZK", new BigDecimal("23.49300")),
                "2024-07-26", Map.of("CZK", new BigDecimal("23.36100"))
        ));
        when(currencyService.getFxRatesTimeSeries("CZK")).thenReturn(mockResponse);

        mockMvc.perform(get("/fx/CZK")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{ base: USD, rates: { " +
                        "2024-07-29: { CZK: 23.45500 }," +
                        "2024-07-30: { CZK: 23.49300 }," +
                        "2024-07-26: { CZK: 23.36100 }}}"));
    }
    @Test
    public void testFxRateByTimeSeriesBadRequest() throws Exception {
        mockMvc.perform(get("/fx/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}