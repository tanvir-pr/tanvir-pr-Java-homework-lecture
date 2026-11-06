package hu.neumann.homework.web;

import hu.neumann.homework.testmenu.CalculatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TestMenuController {

    private final CalculatorService calculatorService;

    public TestMenuController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/test")
    public String form(Model model) {
        model.addAttribute("activeMenu", "test");
        model.addAttribute("a", 10);
        model.addAttribute("b", 5);
        model.addAttribute("operation", "add");
        return "test";
    }

    @PostMapping("/test")
    public String calculate(
            @RequestParam double a,
            @RequestParam double b,
            @RequestParam String operation,
            Model model) {
        model.addAttribute("activeMenu", "test");
        model.addAttribute("a", a);
        model.addAttribute("b", b);
        model.addAttribute("operation", operation);
        try {
            double result = calculatorService.calculate(a, b, operation);
            model.addAttribute("result", result);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "test";
    }
}
