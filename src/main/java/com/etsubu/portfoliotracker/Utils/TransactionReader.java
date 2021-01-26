package com.etsubu.portfoliotracker.Utils;

import com.etsubu.portfoliotracker.Model.Stock;
import com.etsubu.portfoliotracker.Model.Transaction;
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
        try(BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Map<String, Integer> headerIndex = createHeaderMap(br.readLine());
            String line;
            LinkedList<Transaction> transactions = new LinkedList<>();
            log.info("Init");
            while((line = br.readLine()) != null) {
                log.info("line: {}", line);
                transactions.addFirst(parseDegiroTransaction(line, headerIndex));
            }
            return transactions;
        } catch (Exception e) {
            log.error("Failed to read transactions", e);
            throw e;
        }
    }

    private static Map<String, Integer> createHeaderMap(String headerLine) {
        String[] headerParts = headerLine.split(",", -1);
        Map<String, Integer> headerIndex = new HashMap<>();
        for(int i = 0; i < headerParts.length; i++) {
            if(headerParts[i] != null && !headerParts[i].isEmpty()) {
                headerIndex.put(headerParts[i], i);
            }
        }
        return headerIndex;
    }

    private static Transaction parseDegiroTransaction(String line, Map<String, Integer> headerIndex) {
        String[] parts = line.split(",", -1);
        if(parts.length < 15) {
            throw new IllegalArgumentException("Failed parse transaction " + line);
        }
        try {
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
            return new Transaction(ZonedDateTime.parse(date + "" + time, formatter.withZone(ZoneOffset.UTC)),
                    new Stock(isin, name),

                    quantity,
                    price,
                    fees,
                    fxRate,
                    Transaction.DEGIRO_FX_FEE_RATE,
                    orderId,
                    null);
        } catch (Exception e) {
            log.error("Failed to parse degiro transaction");
            throw e;
        }
    }
}
