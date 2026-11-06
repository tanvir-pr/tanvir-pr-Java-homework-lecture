package hu.neumann.homework.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReactPageController {

    @GetMapping("/react")
    public String react(Model model) {
        model.addAttribute("activeMenu", "react");
        return "react";
    }
}
