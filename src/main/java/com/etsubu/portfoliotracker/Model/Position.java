package com.etsubu.portfoliotracker.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Positions keeps track of all the owned shares of the given asset, including different buy orders with different prices
 */
public class Position {
    private static final Num ZERO = PrecisionNum.valueOf(0);
    private static final Logger log = LoggerFactory.getLogger(Position.class);
    private final LinkedList<Order> orders;
    private final Stock stock;

    public Position(Stock stock) {
        this.stock = stock;
        this.orders = new LinkedList<>();
    }

    public List<ClosedTrades> execute(Order order) {
        if(order.getQuantity().isPositive()) {
            buy(order);
            return new LinkedList<>();
        }else {
            return sell(order.getQuantity(), order);
        }
    }

    public Num buy(Order order) {
        log.info("Buying {} shares of {}", order.getQuantity(), order.getStock().getName());
        orders.addLast(order);
        return null;
    }

    public List<ClosedTrades> sell(Num shares, Order o) {
        log.info("Selling {} shares of {}", shares, stock.getName());
        shares = shares.abs();
        List<ClosedTrades> closedTrades = new LinkedList<>();
        boolean addFee = true;
        while(shares.isPositive()) {
            if(orders.isEmpty()) {
                throw new RuntimeException("Tried to sell more shares than what is in the portfolio");
            }
            Order t = orders.getFirst();
            Num sharesSold = t.getQuantity().min(shares);
            // We only add the transaction fee to one of the transactions under this order
            if(addFee) {
                closedTrades.add(new ClosedTrades(t, o, sharesSold, t.sellFxAdjusted(sharesSold, o),
                        Optional.ofNullable(t.getTransactionFee()).orElse(ZERO),
                        Optional.ofNullable(o.getTransactionFee()).orElse(ZERO)));
            } else {
                closedTrades.add(new ClosedTrades(t, o, sharesSold, t.sellFxAdjusted(sharesSold, o), ZERO, ZERO));
            }
            if(t.getQuantity().isZero()) {
                orders.removeFirst();
            }
            shares = shares.minus(sharesSold);
            addFee = false;
        }
        return closedTrades;
    }

    public Num value() {
        return orders.stream().map(x -> x.getPrice().multipliedBy(x.getQuantity()))
                .reduce(ZERO, Num::plus);
    }

    public Num shares() {
        return orders.stream().map(Order::getQuantity).reduce(ZERO, Num::plus);
    }

    @Override
    public String toString() {
        return stock.getName() + " - " + stock.getISIN() + " - " +  "; " + shares() + " => " + value();
    }
}
