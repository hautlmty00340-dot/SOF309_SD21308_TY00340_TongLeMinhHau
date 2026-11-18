package com.aubadminton;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() { return "redirect:/index.html"; }

    // Keep old links opened from src/main/resources/static working.
    @GetMapping("/src/main/resources/static/admin/{fileName:.+}")
    public String redirectOldAdminLink(@PathVariable String fileName) {
        return "redirect:/admin/" + fileName;
    }

    @GetMapping("/src/main/resources/static/{fileName:.+}")
    public String redirectOldStaticLink(@PathVariable String fileName) {
        return "redirect:/" + fileName;
    }
}
