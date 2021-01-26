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
import java.util.LinkedList;
import java.util.List;

public class TransactionReader {
    private static final Logger log = LoggerFactory.getLogger(TransactionReader.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyyHH:mm");

    public static List<Transaction> readTransactionsDegiro(Path file) throws Exception {
        try(BufferedReader br = new BufferedReader(new FileReader(file.toFile(), StandardCharsets.UTF_8))) {
            br.readLine();
            String line;
            LinkedList<Transaction> transactions = new LinkedList<>();
            log.info("Init");
            while((line = br.readLine()) != null) {
                log.info("line: {}", line);
                transactions.addFirst(parseDegiroTransaction(line));
            }
            return transactions;
        } catch (Exception e) {
            log.error("Failed to read transactions", e);
            throw e;
        }
    }

    private static Transaction parseDegiroTransaction(String line) {
        String[] parts = line.split(",", -1);
        if(parts.length < 15) {
            throw new IllegalArgumentException("Failed parse transaction " + line);
        }
        try {
            String date = parts[0];
            String time = parts[1];
            String name = parts[2];
            String isin = parts[3];
            String exchange = parts[4];
            String orderId = parts[parts.length - 1];
            Num quantity = PrecisionNum.valueOf(parts[5]);
            String currency = parts[6];
            Num price = PrecisionNum.valueOf(parts[7]);
            Num fxRate = parts[12].isEmpty() ? null : PrecisionNum.valueOf(parts[12]);
            Num fees = parts[14].isEmpty() ? null : PrecisionNum.valueOf(parts[14]);
            return new Transaction(ZonedDateTime.parse(date + "" + time, formatter.withZone(ZoneOffset.UTC)),
                    new Stock(isin, name, currency),
                    exchange,
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
