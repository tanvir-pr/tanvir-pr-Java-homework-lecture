package hu.neumann.homework.soap;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MnbSoapClient {

    // MNB SOAP POST works on HTTP; HTTPS currently returns 404 for SOAP actions.
    private static final String ENDPOINT = "http://www.mnb.hu/arfolyamok.asmx";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "<(?:\\w+:)?GetExchangeRatesResult>([\\s\\S]*?)</(?:\\w+:)?GetExchangeRatesResult>",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CURRENCIES_RESULT_PATTERN = Pattern.compile(
            "<(?:\\w+:)?GetCurrenciesResult>([\\s\\S]*?)</(?:\\w+:)?GetCurrenciesResult>",
            Pattern.CASE_INSENSITIVE);

    // Do not follow redirects: HTTP->HTTPS redirect would break SOAP POST with 404.
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public List<String> getCurrencies() {
        String body = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <GetCurrencies xmlns="http://www.mnb.hu/webservices/" />
                  </soap:Body>
                </soap:Envelope>
                """;
        try {
            String response = postSoap(body, "http://www.mnb.hu/webservices/GetCurrencies");
            String inner = extractCdataOrGroup(response, CURRENCIES_RESULT_PATTERN);
            if (inner == null || inner.isBlank()) {
                return defaultCurrencies();
            }
            Document doc = parseXml(unescapeXml(inner.trim()));
            NodeList nodes = doc.getElementsByTagName("Curr");
            List<String> currencies = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                currencies.add(nodes.item(i).getTextContent().trim());
            }
            Collections.sort(currencies);
            return currencies.isEmpty() ? defaultCurrencies() : currencies;
        } catch (Exception ex) {
            return defaultCurrencies();
        }
    }

    public List<ExchangeRate> getExchangeRates(LocalDate start, LocalDate end, String currency) {
        String body = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <GetExchangeRates xmlns="http://www.mnb.hu/webservices/">
                      <startDate>%s</startDate>
                      <endDate>%s</endDate>
                      <currencyNames>%s</currencyNames>
                    </GetExchangeRates>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(start.format(DATE), end.format(DATE), currency);

        try {
            String response = postSoap(body, "http://www.mnb.hu/webservices/GetExchangeRates");
            String inner = extractCdataOrGroup(response, RESULT_PATTERN);
            if (inner == null || inner.isBlank()) {
                throw new IllegalStateException("Empty SOAP result from MNB.");
            }
            return parseRates(unescapeXml(inner.trim()), currency);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to call MNB SOAP service: " + ex.getMessage(), ex);
        }
    }

    private String postSoap(String soapBody, String soapAction) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + soapAction + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(soapBody, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " from MNB");
        }
        return response.body();
    }

    private List<ExchangeRate> parseRates(String xml, String currency) throws Exception {
        Document doc = parseXml(xml);
        NodeList days = doc.getElementsByTagName("Day");
        List<ExchangeRate> rates = new ArrayList<>();
        for (int i = 0; i < days.getLength(); i++) {
            Element day = (Element) days.item(i);
            LocalDate date = LocalDate.parse(day.getAttribute("date"), DATE);
            NodeList rateNodes = day.getElementsByTagName("Rate");
            for (int j = 0; j < rateNodes.getLength(); j++) {
                Element rate = (Element) rateNodes.item(j);
                String curr = rate.getAttribute("curr");
                if (currency != null && !currency.equalsIgnoreCase(curr)) {
                    continue;
                }
                double unit = 1.0;
                String unitAttr = rate.getAttribute("unit");
                if (unitAttr != null && !unitAttr.isBlank()) {
                    unit = Double.parseDouble(unitAttr.replace(',', '.'));
                }
                double value = Double.parseDouble(rate.getTextContent().trim().replace(',', '.'));
                rates.add(new ExchangeRate(date, curr, unit, value));
            }
        }
        return rates;
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String extractCdataOrGroup(String soapXml, Pattern pattern) {
        Matcher matcher = pattern.matcher(soapXml);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private String unescapeXml(String value) {
        return value
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&");
    }

    private List<String> defaultCurrencies() {
        return List.of("AUD", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP",
                "HRK", "JPY", "NOK", "PLN", "RON", "RUB", "SEK", "TRY", "USD");
    }
}
