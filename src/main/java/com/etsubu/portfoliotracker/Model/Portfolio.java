package com.etsubu.portfoliotracker.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages a whole portfolio and keeps track of all the positions
 * @author etsubu
 */
public class Portfolio {
    private static final Logger log = LoggerFactory.getLogger(Portfolio.class);
    private final Map<Stock, Position> positions;
    private final List<ClosedTrades> closedTrades;

    public Portfolio() {
        positions = new HashMap<>();
        closedTrades = new LinkedList<>();
    }

    public void executeTransaction(Transaction t) {
        Position p = positions.get(t.getStock());
        if(p == null) {
            p = new Position(t.getStock());
            positions.put(t.getStock(), p);
        }
        closedTrades.addAll(p.execute(new Order(t)));
        if(p.shares().isZero()) {
            log.info("Removing position in {}", t.getStock().getName());
            positions.remove(t.getStock());
        }
    }

    public List<Position> listPositions() {
        return new ArrayList<>(positions.values());
    }

    public List<ClosedTrades> closedTrades() { return new ArrayList<>(closedTrades); }
}
