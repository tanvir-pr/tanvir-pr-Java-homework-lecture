package hu.neumann.homework.web;

import hu.neumann.homework.soap.ExchangeRate;
import hu.neumann.homework.soap.MnbSoapClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SoapController {

    private final MnbSoapClient mnbSoapClient;

    public SoapController(MnbSoapClient mnbSoapClient) {
        this.mnbSoapClient = mnbSoapClient;
    }

    @GetMapping("/soap")
    public String soapForm(Model model) {
        prepareForm(model, "EUR", LocalDate.now().minusDays(14), LocalDate.now());
        return "soap";
    }

    @PostMapping("/soap")
    public String soapSubmit(
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        prepareForm(model, currency, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            model.addAttribute("error", "Start date must be before or equal to end date.");
            return "soap";
        }

        try {
            List<ExchangeRate> rates = mnbSoapClient.getExchangeRates(startDate, endDate, currency);
            model.addAttribute("rates", rates);
            model.addAttribute("chartLabels", rates.stream().map(r -> r.getDate().toString()).toList());
            model.addAttribute("chartValues", rates.stream().map(ExchangeRate::getValue).toList());
            if (rates.isEmpty()) {
                model.addAttribute("error", "No exchange rates returned for the selected period.");
            }
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "soap";
    }

    private void prepareForm(Model model, String currency, LocalDate start, LocalDate end) {
        model.addAttribute("activeMenu", "soap");
        model.addAttribute("currencies", mnbSoapClient.getCurrencies());
        model.addAttribute("currency", currency);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
    }
}
