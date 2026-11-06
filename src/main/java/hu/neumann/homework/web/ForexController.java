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

    private void prepareInstrumentPage(Model model, String activeMenu, String instrument) {
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("instruments", OandaClient.INSTRUMENTS);
        model.addAttribute("instrument", instrument);
    }

    private String missingConfigMessage() {
        return "Oanda credentials missing. Copy password.txt.example to password.txt and set OANDA_API_TOKEN and OANDA_ACCOUNT_ID.";
    }
}
