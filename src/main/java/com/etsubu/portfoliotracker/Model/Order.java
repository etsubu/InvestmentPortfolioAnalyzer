package com.etsubu.portfoliotracker.Model;

import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.num.Num;

@Getter
@ToString
public class Order extends Transaction{
    private static final Logger log = LoggerFactory.getLogger(Order.class);
    private final Num initialAmount;

    public Order(Transaction t) {
        super(t.getTime(), t.getStock(), t.getExchange(), t.getQuantity(), t.getPrice(), t.getTransactionFee(), t.getFxRate(), t.getFxFeeRate(), t.getOrderId(), null);
        initialAmount = this.quantity;
    }

    public Num sellFxAdjusted(Num amount, Order t) {
        if(amount.isGreaterThan(this.quantity)) {
            throw new IllegalArgumentException("Tried to sell too many shares from order " + amount + " > " + this.quantity.intValue());
        }
        this.quantity = this.quantity.minus(amount);
        Num sellNetPrice =  amount.multipliedBy(t.getPrice());
        Num buyNetPrice = amount.multipliedBy(this.price);
        if(t.getFxRate() != null) {
            buyNetPrice = buyNetPrice.dividedBy(this.fxRate);
            log.info("Quantity: {}, fx: {}, price: {}, new price: {}, initial value: {}, {}", quantity, t.getFxRate(), t.getPrice(), getPrice(), buyNetPrice, getTransactionFee());
            return (sellNetPrice.dividedBy(t.getFxRate())).minus(buyNetPrice);
        }
        return sellNetPrice.minus(buyNetPrice);
    }
}
