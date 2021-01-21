package com.etsubu.portfoliotracker.API.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ta4j.core.BarSeries;

@Getter
@AllArgsConstructor
public class PriceSeries {
    private final BarSeries series;
    private final String currency;
}
