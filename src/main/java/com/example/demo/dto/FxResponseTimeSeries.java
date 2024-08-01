package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Map;

@Setter
@Getter
public class FxResponseTimeSeries {
    private String base;
    private Map<String, Map<String, BigDecimal>> rates;
}
