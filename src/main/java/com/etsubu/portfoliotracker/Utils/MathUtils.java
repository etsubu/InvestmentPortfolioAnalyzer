package com.etsubu.portfoliotracker.Utils;

import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MathUtils {

    public static Num round(Num value) {
        BigDecimal bigDecimal = new BigDecimal(value.toString());
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
        return PrecisionNum.valueOf(bigDecimal, 2);
    }

    public static String format(Num value) {
        DecimalFormat f = new DecimalFormat("##.00");  // this will helps you to always keeps in two decimal places
        return (f.format(value.doubleValue()));
    }
}
