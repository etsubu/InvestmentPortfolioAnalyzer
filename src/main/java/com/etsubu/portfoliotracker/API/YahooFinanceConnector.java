package com.etsubu.portfoliotracker.API;

import com.etsubu.portfoliotracker.API.Model.*;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.PrecisionNum;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class YahooFinanceConnector {
    private static final Logger log = LoggerFactory.getLogger(YahooFinanceConnector.class);
    private static final String PRICE_DATA_TIMESERIES_URL =  "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&includeAdjustedClose=true&period1=%d&period2=%d";
    private final HttpClient httpClient;
    private final Gson gson;
    private Instant lastRequest;

    public YahooFinanceConnector() throws IOException {
        gson = new Gson();
        httpClient = HttpClient.newBuilder().build();
        log.info("{} initialized", getClass().getName());
    }

    private Optional<String> sendRestRequest(String url) throws InterruptedException, YahooFinanceException, IOException {
        if(lastRequest != null && lastRequest.plusMillis(1000).isAfter(Instant.now())) {
            Instant sleepUntil = lastRequest.plusMillis(1010); // 1s + 10millis, extra 10 millis for safety
            // Sleep until 1s has passed since last request
            while(Instant.now().isBefore(sleepUntil)) {
                try {
                    Thread.sleep(sleepUntil.minusMillis(sleepUntil.toEpochMilli()).toEpochMilli());
                } catch (InterruptedException ignored) {}
            }
        }
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20L))
                .build();
        lastRequest = Instant.now();
        log.info("Sending http GET to {}", url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            log.error("Received http status code {}, body: {}", response.statusCode(), response.body());
            try {
                JSONObject root = new JSONObject(response.body());
                for (String key : root.keySet()) {
                    JSONObject obj = root.getJSONObject(key);
                    if (obj.has("error")) {
                        throw new YahooFinanceException(response.statusCode(), gson.fromJson(obj.getJSONObject("error").toString(), ErrorEntry.class));
                    }
                }
            } catch (JSONException e) {
                log.error("Received error could not be parsed as json '{}'", response.body());
                throw new YahooFinanceException(response.statusCode(), new ErrorEntry(Integer.toString(response.statusCode()), response.body()));
            }
        }
        return Optional.of(response.body());
    }

    public PriceDataSeries queryPriceData(Instant from, String ticker) throws IOException, InterruptedException, YahooFinanceException {
        long fromEpoch = Optional.ofNullable(from).map(Instant::getEpochSecond).orElse(0L);
        String url = String.format(PRICE_DATA_TIMESERIES_URL, URLEncoder.encode(ticker, StandardCharsets.UTF_8), fromEpoch, Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond());
        Optional<String> response = sendRestRequest(url);
        if(response.isEmpty()) {
            throw new IOException("Yahoo finance did not return price data");
        }
        JSONObject root = new JSONObject(response.get());
        root = root.getJSONObject("chart");
        if(root.get("error") != null && !root.get("error").toString().equalsIgnoreCase("null")) {
            log.error("Received error from yahoo finance rest api '{}', {}", root.get("error").toString(), response.get());
            throw new IllegalArgumentException("Yahoo finance returned error " + root.get("error").toString());
        }
        return gson.fromJson(root.getJSONArray("result").get(0).toString(), PriceDataSeries.class);
    }

    public PriceSeries queryBarSeries(Instant from, String ticker) throws IOException, InterruptedException, YahooFinanceException {
        PriceDataSeries series = queryPriceData(from, ticker);
        String currency = Optional.ofNullable(series.getMeta().getCurrency()).orElse(null);
        if(series.getTimestamp() == null) {
            log.info("No price data available for {}", ticker);
            return null;
        }
        BaseBarSeries barSeries = new BaseBarSeries(ticker);
        OLHCSeries olhcSeries = series.getIndicators().getQuote().get(0);
        AdjustedCloseQuote adjCloseSeries = series.getIndicators().getAdjclose().get(0);
        for(int i = 0; i < series.getTimestamp().size(); i++) {
            Instant time = Instant.ofEpochSecond(Long.parseLong(series.getTimestamp().get(i)));
            String o = olhcSeries.getOpen().get(i);
            String l = olhcSeries.getHigh().get(i);
            String h = olhcSeries.getHigh().get(i);
            String c = olhcSeries.getClose().get(i);
            String v = olhcSeries.getVolume().get(i);
            String adjC = adjCloseSeries.getAdjclose().get(i);
            if(o != null && l != null && c != null && h != null && v != null) {
                CloseBar bar = new CloseBar(Duration.ofDays(1),
                        time.atZone(ZoneOffset.UTC),
                        PrecisionNum.valueOf(o),
                        PrecisionNum.valueOf(h),
                        PrecisionNum.valueOf(l),
                        PrecisionNum.valueOf(c),
                        PrecisionNum.valueOf(v),
                        PrecisionNum.valueOf(adjC),
                        PrecisionNum.valueOf(1));
                barSeries.addBar(bar);
            }
        }
        return new PriceSeries(barSeries, currency);
    }
}
