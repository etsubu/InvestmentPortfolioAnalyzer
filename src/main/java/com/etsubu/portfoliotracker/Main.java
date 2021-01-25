package com.etsubu.portfoliotracker;

import com.etsubu.portfoliotracker.Model.Portfolio;
import com.etsubu.portfoliotracker.Model.Transaction;
import com.etsubu.portfoliotracker.Utils.TransactionReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Path transactionsFile = Path.of(args.length > 0 ? args[0] : "transactions.csv");
        if(Files.notExists(transactionsFile)) {
            System.out.println("Transactions file does not exist!");
            System.exit(0);
        }
        List<Transaction> transactions = TransactionReader.readTransactionsDegiro(transactionsFile);
        Portfolio portfolio = new Portfolio();
        for(Transaction t : transactions) {
            portfolio.executeTransaction(t);
        }
        portfolio.listPositions().forEach(System.out::println);
        portfolio.write9ATaxReports();
    }
}
