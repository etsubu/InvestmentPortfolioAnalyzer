package com.etsubu.portfoliotracker.Model;

import com.etsubu.portfoliotracker.Utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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

    public void write9ATaxReports() {
        Set<Integer> years = closedTrades.stream().map(x -> x.getSellDate().getYear()).collect(Collectors.toSet());
        years.forEach(x -> write9ATaxReport(Path.of("9A - " + x+".csv"), x));
    }

    public boolean write9ATaxReport(Path file, int year) {
        List<ClosedTrades> trades = closedTrades.stream().filter(x -> x.getSellDate().getYear() == year).collect(Collectors.toList());
        Num totalGains = (trades.stream().map(ClosedTrades::getCostAdjustedGain).filter(Num::isPositive).reduce(PrecisionNum.valueOf(0), Num::plus));
        Num totalLosses = (trades.stream().map(ClosedTrades::getCostAdjustedGain).filter(Num::isNegative).reduce(PrecisionNum.valueOf(0), Num::plus));
        Num netGain = (totalGains.plus(totalLosses));
        try(BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            bw.write("Yhteenveto");
            bw.newLine();
            bw.write("Voitot yhteensä,Tappiot yhteensä,Voitot/Tappiot valitut");
            bw.newLine();
            bw.write(MathUtils.format(totalGains) + "," + MathUtils.format(totalLosses) + "," + MathUtils.format(netGain));
            bw.newLine();
            bw.newLine();
            bw.write("9A – Luovutusvoitot ja- tappio");
            bw.newLine();
            bw.write("1.1." + year + " - 31.12." + year);
            bw.newLine();
            bw.write("Luovutettu arvopaperi/arvo-osuus,ISIN,Määrä,Hankinta-aika,Luovutusaika,Luovutushinta,Hankintahinta,Hankintakulut,Myyntikulut,Voitto tai tappio");
            bw.newLine();
            for(ClosedTrades ct : closedTrades.stream().filter(x -> x.getSellDate().getYear() == year).collect(Collectors.toList())) {
                bw.write(ct.getAsCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            log.error("Failed to write 9A report", e);
            return false;
        }
        return true;
    }

    public List<Position> listPositions() {
        return new ArrayList<>(positions.values());
    }

    public List<ClosedTrades> closedTrades() { return new ArrayList<>(closedTrades); }
}
