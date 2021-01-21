package com.etsubu.portfoliotracker.API.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PriceDataSeries {
    private final PriceMeta meta;
    private final List<String> timestamp;
    private final IndicatorsSeries indicators;
}
