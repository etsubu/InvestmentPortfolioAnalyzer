package com.etsubu.portfoliotracker.API.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdjustedCloseQuote {
    private final List<String> adjclose;
}
