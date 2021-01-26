package com.etsubu.portfoliotracker.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.time.ZonedDateTime;
import java.util.Optional;

@Getter
@AllArgsConstructor
@ToString
public class Transaction {
    private static final Num ONE = PrecisionNum.valueOf(1);
    public static final Num DEGIRO_FX_FEE_RATE = PrecisionNum.valueOf(0.001); // 0.1%
    protected ZonedDateTime time;
    protected Stock stock;
    protected Num quantity;
    protected Num price;
    protected Num transactionFee;
    protected Num fxRate;
    protected Num fxFeeRate;
    protected String orderId;
    protected Num gain;

    public Num getFxFee() {
        if(fxRate == null) {
            return PrecisionNum.valueOf(0);
        }
        Num transactionValue = quantity.multipliedBy(price);
        Num grossValue = transactionValue.multipliedBy(fxRate);
        Num netValue = transactionValue.multipliedBy(fxRate.multipliedBy(ONE.minus(fxFeeRate)));
        return grossValue.minus(netValue);
    }

    public Num netPrice() {
        if(fxRate == null) {
            return quantity.multipliedBy(price);
        }
        return quantity.multipliedBy(price).dividedBy(fxRate);
    }

    public void setGain(Num gain) {
        this.gain = gain;
    }

    public String asCsv() {
        return time.toString() + "," + stock.getName() + "," + stock.getISIN() + "," + quantity + "," + price + "," + fxRate + "," + transactionFee + "," + (gain != null ? gain : "0");
    }
}
