package hu.neumann.homework.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

    @GetMapping("/forex/histprice")
    public String forexHistPrice(Model model) {
        return placeholder(model, "forex-histprice", "Forex-HistPrice",
                "Historical prices for the selected instrument and granularity.");
    }

    @GetMapping("/forex/open")
    public String forexOpen(Model model) {
        return placeholder(model, "forex-open", "Forex-Open",
                "Open a long or short market position.");
    }

    @GetMapping("/forex/pos")
    public String forexPos(Model model) {
        return placeholder(model, "forex-pos", "Forex-Pos",
                "Open positions will be listed in a table here.");
    }

    @GetMapping("/forex/close")
    public String forexClose(Model model) {
        return placeholder(model, "forex-close", "Forex-Close",
                "Close a position by TradeId at market price.");
    }

    @GetMapping("/test")
    public String testMenu(Model model) {
        return placeholder(model, "test", "Test",
                "Calculator-style form and automated tests will be added here.");
    }

    @GetMapping("/react")
    public String reactMenu(Model model) {
        return placeholder(model, "react", "React",
                "React CRUD application will be served from this menu.");
    }

    private String placeholder(Model model, String activeMenu, String title, String message) {
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageMessage", message);
        return "placeholder";
    }
}
