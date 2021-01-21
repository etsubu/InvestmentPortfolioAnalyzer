package com.etsubu.portfoliotracker.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Stock {
    private final String ISIN;
    private final String name;
    private final String currency;

    @Override
    public int hashCode() {
        return ISIN.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass()) {
            return false;
        }
        return ISIN.equals(((Stock)o).getISIN());
    }
}
