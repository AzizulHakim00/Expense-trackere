package com.example.expensetracker.controller;

import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {
    private final UserService users;
    public PageController(UserService users) { this.users=users; }
    public record Registration(@NotBlank @Size(max=80) String name,
                               @NotBlank @Email String email,
                               @NotBlank @Size(min=8,max=72) String password) {}

    @GetMapping("/") String index() { return "redirect:/dashboard"; }
    @GetMapping("/login") String login() { return "login"; }
    @GetMapping("/register") String register(Model model) {
        if (!model.containsAttribute("registration")) model.addAttribute("registration",new Registration("","",""));
        return "register";
    }
    @PostMapping("/register") String register(@Valid @ModelAttribute("registration") Registration registration,
        BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) return "register";
        try { users.register(registration.name(),registration.email(),registration.password()); }
        catch (IllegalArgumentException ex) { errors.rejectValue("email","duplicate",ex.getMessage()); return "register"; }
        redirect.addFlashAttribute("registered",true); return "redirect:/login";
    }
    @GetMapping("/dashboard") String dashboard(Authentication authentication,Model model) {
        User user=users.current(authentication.getName());
        model.addAttribute("name",user.name); return "dashboard";
    }
}
