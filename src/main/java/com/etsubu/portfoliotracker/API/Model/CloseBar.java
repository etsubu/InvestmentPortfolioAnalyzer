package com.etsubu.portfoliotracker.API.Model;

import org.ta4j.core.BaseBar;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class CloseBar extends BaseBar {
    private Num adjClose;

    public CloseBar(Duration timePeriod, ZonedDateTime endTime, Function<Number, Num> numFunction) {
        super(timePeriod, endTime, numFunction);
    }

    public CloseBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice,
                   Num closePrice, Num volume, Num adjClose, Num amount) {
        super(timePeriod, endTime, openPrice, highPrice, lowPrice, closePrice, volume, amount, 0);
        this.adjClose = adjClose;
    }

    public Num getAdjClose() { return adjClose; }
}
