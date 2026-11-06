package hu.neumann.homework.forex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OandaClient {

    public static final List<String> INSTRUMENTS = List.of(
            "EUR_USD", "GBP_USD", "USD_JPY", "AUD_USD", "USD_CAD",
            "USD_CHF", "NZD_USD", "EUR_GBP", "EUR_HUF", "USD_HUF"
    );

    public static final List<String> GRANULARITIES = List.of(
            "S5", "S10", "S15", "S30", "M1", "M5", "M15", "M30",
            "H1", "H4", "D", "W", "M"
    );

    private final OandaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public OandaClient(OandaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Map<String, String> getAccountSummary() {
        JsonNode root = getJson("/v3/accounts/" + encode(properties.getAccountId()) + "/summary");
        JsonNode account = root.path("account");
        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("id", text(account, "id"));
        summary.put("alias", text(account, "alias"));
        summary.put("currency", text(account, "currency"));
        summary.put("balance", text(account, "balance"));
        summary.put("unrealizedPL", text(account, "unrealizedPL"));
        summary.put("NAV", text(account, "NAV"));
        summary.put("marginUsed", text(account, "marginUsed"));
        summary.put("marginAvailable", text(account, "marginAvailable"));
        summary.put("openTradeCount", text(account, "openTradeCount"));
        summary.put("openPositionCount", text(account, "openPositionCount"));
        return summary;
    }

    public Map<String, String> getActualPrice(String instrument) {
        String path = "/v3/accounts/" + encode(properties.getAccountId())
                + "/pricing?instruments=" + encode(instrument);
        JsonNode root = getJson(path);
        JsonNode price = root.path("prices").path(0);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("instrument", text(price, "instrument"));
        result.put("time", text(price, "time"));
        result.put("bid", firstLiquidityPrice(price.path("bids")));
        result.put("ask", firstLiquidityPrice(price.path("asks")));
        String bid = result.get("bid");
        String ask = result.get("ask");
        if (!bid.isBlank() && !ask.isBlank()) {
            double mid = (Double.parseDouble(bid) + Double.parseDouble(ask)) / 2.0;
            result.put("mid", String.format("%.5f", mid));
        } else {
            result.put("mid", "");
        }
        return result;
    }

    public List<Map<String, String>> getHistoricalPrices(String instrument, String granularity) {
        String path = "/v3/instruments/" + encode(instrument)
                + "/candles?granularity=" + encode(granularity)
                + "&count=10&price=M";
        JsonNode root = getJson(path);
        List<Map<String, String>> rows = new ArrayList<>();
        for (JsonNode candle : root.path("candles")) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("time", text(candle, "time"));
            row.put("complete", text(candle, "complete"));
            row.put("volume", text(candle, "volume"));
            JsonNode mid = candle.path("mid");
            row.put("open", text(mid, "o"));
            row.put("high", text(mid, "h"));
            row.put("low", text(mid, "l"));
            row.put("close", text(mid, "c"));
            rows.add(row);
        }
        return rows;
    }

    public Map<String, String> openMarketOrder(String instrument, double units) {
        String path = "/v3/accounts/" + encode(properties.getAccountId()) + "/orders";
        String json = """
                {
                  "order": {
                    "type": "MARKET",
                    "instrument": "%s",
                    "units": "%s",
                    "timeInForce": "FOK",
                    "positionFill": "DEFAULT"
                  }
                }
                """.formatted(instrument, trimUnits(units));
        JsonNode root = postJson(path, json);
        Map<String, String> result = new LinkedHashMap<>();
        JsonNode fill = root.path("orderFillTransaction");
        if (!fill.isMissingNode()) {
            result.put("tradeId", firstTradeId(fill));
            result.put("instrument", text(fill, "instrument"));
            result.put("units", text(fill, "units"));
            result.put("price", text(fill, "price"));
            result.put("time", text(fill, "time"));
            result.put("pl", text(fill, "pl"));
        } else {
            result.put("raw", root.toString());
        }
        return result;
    }

    public List<Map<String, String>> getOpenTrades() {
        String path = "/v3/accounts/" + encode(properties.getAccountId()) + "/openTrades";
        JsonNode root = getJson(path);
        List<Map<String, String>> rows = new ArrayList<>();
        for (JsonNode trade : root.path("trades")) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("id", text(trade, "id"));
            row.put("instrument", text(trade, "instrument"));
            row.put("price", text(trade, "price"));
            row.put("currentUnits", text(trade, "currentUnits"));
            row.put("unrealizedPL", text(trade, "unrealizedPL"));
            row.put("openTime", text(trade, "openTime"));
            rows.add(row);
        }
        return rows;
    }

    public Map<String, String> closeTrade(String tradeId) {
        String path = "/v3/accounts/" + encode(properties.getAccountId())
                + "/trades/" + encode(tradeId) + "/close";
        JsonNode root = putJson(path, "{}");
        Map<String, String> result = new LinkedHashMap<>();
        JsonNode close = root.path("orderFillTransaction");
        if (close.isMissingNode()) {
            close = root.path("orderCancelTransaction");
        }
        result.put("tradeId", tradeId);
        result.put("price", text(close, "price"));
        result.put("units", text(close, "units"));
        result.put("pl", text(close, "pl"));
        result.put("time", text(close, "time"));
        if (result.get("price").isBlank()) {
            result.put("raw", root.toString());
        }
        return result;
    }

    private JsonNode getJson(String path) {
        return send("GET", path, null);
    }

    private JsonNode postJson(String path, String body) {
        return send("POST", path, body);
    }

    private JsonNode putJson(String path, String body) {
        return send("PUT", path, body);
    }

    private JsonNode send(String method, String path, String body) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException(
                    "Oanda is not configured. Create password.txt from password.txt.example with OANDA_API_TOKEN and OANDA_ACCOUNT_ID.");
        }
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + path))
                    .header("Authorization", "Bearer " + properties.getApiToken())
                    .header("Content-Type", "application/json")
                    .header("Accept-Datetime-Format", "RFC3339");

            if ("POST".equals(method)) {
                builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            } else if ("PUT".equals(method)) {
                builder.PUT(HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
            } else {
                builder.GET();
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Oanda API HTTP " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Oanda API call failed: " + ex.getMessage(), ex);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText();
    }

    private String firstLiquidityPrice(JsonNode arr) {
        if (arr.isArray() && !arr.isEmpty()) {
            return text(arr.get(0), "price");
        }
        return "";
    }

    private String firstTradeId(JsonNode fill) {
        JsonNode tradeOpened = fill.path("tradeOpened");
        if (!tradeOpened.isMissingNode()) {
            return text(tradeOpened, "tradeID");
        }
        JsonNode tradesOpened = fill.path("tradesOpened");
        if (tradesOpened.isArray() && !tradesOpened.isEmpty()) {
            return text(tradesOpened.get(0), "tradeID");
        }
        return text(fill, "id");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String trimUnits(double units) {
        if (units == Math.rint(units)) {
            return String.valueOf((long) units);
        }
        return String.valueOf(units);
    }
}
