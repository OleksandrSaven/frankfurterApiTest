package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FxResponseTimeSeries {
    private String base;
    private Map<String, Map<String, BigDecimal>> rates;
}
