package com.etsubu.portfoliotracker;

import com.etsubu.portfoliotracker.Model.ClosedTrades;
import com.etsubu.portfoliotracker.Model.Portfolio;
import com.etsubu.portfoliotracker.Model.Transaction;
import com.etsubu.portfoliotracker.Utils.TransactionReader;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = TransactionReader.readTransactionsDegiro(Paths.get("transactions.csv"));
        Portfolio portfolio = new Portfolio();
        for(Transaction t : transactions) {
            portfolio.executeTransaction(t);
        }
        portfolio.listPositions().forEach(System.out::println);
        //Files.writeString(Path.of("track.csv"), transactions.stream().map(Transaction::asCsv).collect(Collectors.joining("\n")));
        //System.out.println(transactions.stream().map(Transaction::getGain).filter(Objects::nonNull).reduce(Num::plus).get());
        System.out.println("************");
        portfolio.closedTrades().forEach(x -> System.out.println(x));
        Files.writeString(Path.of("track.csv"), portfolio.closedTrades().stream().map(ClosedTrades::getAsCsv).collect(Collectors.joining("\n")));
    }
}
