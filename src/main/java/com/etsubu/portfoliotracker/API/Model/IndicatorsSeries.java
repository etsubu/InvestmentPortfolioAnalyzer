package com.etsubu.portfoliotracker.API.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class IndicatorsSeries {
    private final List<OLHCSeries> quote;
    private final List<AdjustedCloseQuote> adjclose;
}
