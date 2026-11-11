package kz.iitu.springlab.web;

import kz.iitu.springlab.user.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Variant 2: GET /register shows the form, POST /register creates a USER account
@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registration;

    public RegistrationController(RegistrationService registration) {
        this.registration = registration;
    }

    @GetMapping
    public String form() {
        return "register";
    }

    @PostMapping
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        String name = username.trim();
        if (!name.matches("[A-Za-z0-9_.-]{3,60}") || password.length() < 6) {
            model.addAttribute("error", "Username: 3-60 letters, digits, '_', '.', '-'; password: at least 6 characters");
            model.addAttribute("username", name);
            return "register";
        }
        try {
            registration.register(name, password);
        } catch (RegistrationService.UsernameTakenException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", name);
            return "register";
        }
        return "redirect:/login?registered";
    }
}
