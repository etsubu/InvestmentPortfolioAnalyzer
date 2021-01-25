package com.etsubu.portfoliotracker.Model;

import com.etsubu.portfoliotracker.Utils.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Getter
@AllArgsConstructor
@ToString
public class ClosedTrades {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // Finnish notation requested by tax administration
    private final Order buyOrder;
    private final Order sellOrder;
    private final Num quantity;
    private final Num gain;
    private final Num buyFee;
    private final Num sellFee;

    private Num getTransactionSum(Order order) {
        return Optional.ofNullable(order.getFxRate())
                .map(x -> MathUtils.round(quantity.abs().multipliedBy(order.getPrice()).dividedBy(x)))
                .orElseGet(() -> quantity.abs().multipliedBy(order.getPrice()));
    }

    public Num getBuyAmount() {
        return getTransactionSum(buyOrder);
    }

    public Num getSellAmount() {
        return getTransactionSum(sellOrder);
    }

    public ZonedDateTime getBuyDate() { return buyOrder.getTime(); }

    public ZonedDateTime getSellDate() { return sellOrder.getTime(); }

    public Num getBuyFees() { return buyOrder.getTransactionFee(); }

    public Num getSellFees() { return sellOrder.getTransactionFee(); }

    public Num getCostAdjustedGain() {
        return gain.plus(buyFee).plus(sellFee);
    }

    public String getAsCsv() {
        return buyOrder.getStock().getName()
                + "," + buyOrder.getStock().getISIN()
                + "," + getQuantity()
                + "," + formatter.format(getBuyDate())
                + "," + formatter.format(getSellDate())
                + "," + MathUtils.format(getSellAmount())
                + "," + MathUtils.format(getBuyAmount())
                + "," + buyFee
                + "," + sellFee
                + "," + MathUtils.format(getCostAdjustedGain());
    }
}
