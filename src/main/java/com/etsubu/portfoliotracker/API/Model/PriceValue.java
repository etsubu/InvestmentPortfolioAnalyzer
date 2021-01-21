package com.etsubu.portfoliotracker.API.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class PriceValue {
    private final String raw;
    private final String fmt;
}
