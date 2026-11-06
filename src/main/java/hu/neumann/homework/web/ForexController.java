package hu.neumann.homework.web;

import hu.neumann.homework.forex.OandaClient;
import hu.neumann.homework.forex.OandaProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForexController {

    private final OandaClient oandaClient;
    private final OandaProperties oandaProperties;

    public ForexController(OandaClient oandaClient, OandaProperties oandaProperties) {
        this.oandaClient = oandaClient;
        this.oandaProperties = oandaProperties;
    }

    @GetMapping("/forex/account")
    public String account(Model model) {
        model.addAttribute("activeMenu", "forex-account");
        if (!oandaProperties.isConfigured()) {
            model.addAttribute("error", missingConfigMessage());
            return "forex/account";
        }
        try {
            model.addAttribute("account", oandaClient.getAccountSummary());
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "forex/account";
    }

    @GetMapping("/forex/actprice")
    public String actPriceForm(Model model) {
        prepareInstrumentPage(model, "forex-actprice", "EUR_USD");
        return "forex/actprice";
    }

    @PostMapping("/forex/actprice")
    public String actPriceSubmit(@RequestParam String instrument, Model model) {
        prepareInstrumentPage(model, "forex-actprice", instrument);
        if (!oandaProperties.isConfigured()) {
            model.addAttribute("error", missingConfigMessage());
            return "forex/actprice";
        }
        try {
            model.addAttribute("price", oandaClient.getActualPrice(instrument));
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "forex/actprice";
    }

    @GetMapping("/forex/histprice")
    public String histPriceForm(Model model) {
        prepareHistPage(model, "EUR_USD", "D");
        return "forex/histprice";
    }

    @PostMapping("/forex/histprice")
    public String histPriceSubmit(
            @RequestParam String instrument,
            @RequestParam String granularity,
            Model model) {
        prepareHistPage(model, instrument, granularity);
        if (!oandaProperties.isConfigured()) {
            model.addAttribute("error", missingConfigMessage());
            return "forex/histprice";
        }
        try {
            model.addAttribute("candles", oandaClient.getHistoricalPrices(instrument, granularity));
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "forex/histprice";
    }

    @GetMapping("/forex/open")
    public String openForm(Model model) {
        prepareOpenPage(model, "EUR_USD", 100);
        return "forex/open";
    }

    @PostMapping("/forex/open")
    public String openSubmit(
            @RequestParam String instrument,
            @RequestParam double units,
            Model model) {
        prepareOpenPage(model, instrument, units);
        if (!oandaProperties.isConfigured()) {
            model.addAttribute("error", missingConfigMessage());
            return "forex/open";
        }
        if (units == 0) {
            model.addAttribute("error", "Quantity must not be zero. Positive = Long, negative = Short.");
            return "forex/open";
        }
        try {
            model.addAttribute("result", oandaClient.openMarketOrder(instrument, units));
            model.addAttribute("success", "Market order submitted.");
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "forex/open";
    }

    @GetMapping("/forex/pos")
    public String positions(Model model) {
        model.addAttribute("activeMenu", "forex-pos");
        if (!oandaProperties.isConfigured()) {
            model.addAttribute("error", missingConfigMessage());
            return "forex/pos";
        }
        try {
            model.addAttribute("trades", oandaClient.getOpenTrades());
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "forex/pos";
    }

    @GetMapping("/forex/close")
    public String closeForm(Model model) {
        model.addAttribute("activeMenu", "forex-close");
        model.addAttribute("tradeId", "");
        return "forex/close";
    }

    @PostMapping("/forex/close")
    public String closeSubmit(@RequestParam String tradeId, Model model) {
        model.addAttribute("activeMenu", "forex-close");
        model.addAttribute("tradeId", tradeId);
        if (!oandaProperties.isConfigured()) {
            model.addAttribute("error", missingConfigMessage());
            return "forex/close";
        }
        if (tradeId == null || tradeId.isBlank()) {
            model.addAttribute("error", "TradeId is required.");
            return "forex/close";
        }
        try {
            model.addAttribute("result", oandaClient.closeTrade(tradeId.trim()));
            model.addAttribute("success", "Close request submitted for trade " + tradeId.trim());
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "forex/close";
    }

    private void prepareInstrumentPage(Model model, String activeMenu, String instrument) {
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("instruments", OandaClient.INSTRUMENTS);
        model.addAttribute("instrument", instrument);
    }

    private void prepareHistPage(Model model, String instrument, String granularity) {
        prepareInstrumentPage(model, "forex-histprice", instrument);
        model.addAttribute("granularities", OandaClient.GRANULARITIES);
        model.addAttribute("granularity", granularity);
    }

    private void prepareOpenPage(Model model, String instrument, double units) {
        prepareInstrumentPage(model, "forex-open", instrument);
        model.addAttribute("units", units);
    }

    private String missingConfigMessage() {
        return "Oanda credentials missing. Copy password.txt.example to password.txt and set OANDA_API_TOKEN and OANDA_ACCOUNT_ID.";
    }
}
