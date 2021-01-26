package com.etsubu.portfoliotracker.Utils;

import com.etsubu.portfoliotracker.Model.Stock;
import com.etsubu.portfoliotracker.Model.Transaction;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionReader {
    private static final Logger log = LoggerFactory.getLogger(TransactionReader.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyyHH:mm");
    private static final String DATE = "Päiväys";
    private static final String TIME = "Aika";
    private static final String NAME = "Tuote";
    private static final String ISIN = "ISIN";
    private static final String EXCHANGE = "Reference exchange";
    private static final String QUANTITY = "Quantity";
    private static final String PRICE = "Kurssi";
    private static final String MARKET_VALUE = "Markkina-arvo";
    private static final String FX_RATE = "Vaihtokurssi";
    private static final String TRANSACTION_FEE = "Transaction";
    private static final String ORDER_ID = "Order ID";


    public static List<Transaction> readTransactionsDegiro(Path file) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(file.toFile(), StandardCharsets.UTF_8))) {
            String[] lineInArray;
            Map<String, Integer> headerIndex = createHeaderMap(reader.readNext());
            LinkedList<Transaction> transactions = new LinkedList<>();
            log.info("Init");
            while ((lineInArray = reader.readNext()) != null) {
                log.info("Line: {}", String.join("-", lineInArray));
                parseDegiroTransaction(lineInArray, headerIndex).ifPresent(transactions::addFirst);
            }
            return transactions;
        } catch (Exception e) {
            log.error("Failed to read transactions", e);
            throw e;
        }
    }

    private static Map<String, Integer> createHeaderMap(String[] headerParts) {
        Map<String, Integer> headerIndex = new HashMap<>();
        for(int i = 0; i < headerParts.length; i++) {
            if(headerParts[i] != null && !headerParts[i].isEmpty()) {
                headerIndex.put(headerParts[i], i);
            }
        }
        return headerIndex;
    }

    private static Optional<Transaction> parseDegiroTransaction(String[] parts, Map<String, Integer> headerIndex) {
        try {
            if(parts[headerIndex.get(DATE)].isEmpty()) {
                log.warn("Received broken line with no date. Skipping: {}", String.join(",", parts));
                return Optional.empty();
            }
            String date = parts[headerIndex.get(DATE)];
            String time = parts[headerIndex.get(TIME)];
            String name = parts[headerIndex.get(NAME)];
            String isin = parts[headerIndex.get(ISIN)];
            //String exchange = parts[headerIndex.get(EXCHANGE)];
            String orderId = parts[headerIndex.get(ORDER_ID)];
            Num quantity = PrecisionNum.valueOf(parts[headerIndex.get(QUANTITY)]);
            //String currency = parts[6];
            Num price = PrecisionNum.valueOf(parts[headerIndex.get(PRICE)]);
            Num fxRate = Optional.ofNullable(headerIndex.get(FX_RATE)).map(x -> parts[x].isEmpty() ? null : PrecisionNum.valueOf(parts[x])).orElse(null);
            Num fees = Optional.ofNullable(headerIndex.get(TRANSACTION_FEE)).map(x -> parts[x].isEmpty() ? null : PrecisionNum.valueOf(parts[x])).orElse(null);
            return Optional.of(new Transaction(ZonedDateTime.parse(date + "" + time, formatter.withZone(ZoneOffset.UTC)),
                    new Stock(isin, name),

                    quantity,
                    price,
                    fees,
                    fxRate,
                    Transaction.DEGIRO_FX_FEE_RATE,
                    orderId,
                    null));
        } catch (Exception e) {
            log.error("Failed to parse degiro transaction");
            throw e;
        }
    }
}
