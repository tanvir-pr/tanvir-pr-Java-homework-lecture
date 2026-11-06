package hu.neumann.homework.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

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
