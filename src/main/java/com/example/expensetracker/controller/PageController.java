package com.example.expensetracker.controller;

import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public record ProfileForm(@NotBlank @Size(max=80) String name,
                              @NotBlank @Email @Size(max=160) String email) {}

    @GetMapping("/")
    String index(Authentication authentication) {
        User user = users.current(authentication.getName());
        return users.isAdmin(user) ? "redirect:/admin/dashboard" : "redirect:/dashboard";
    }

    @GetMapping("/login") String login() { return "login"; }

    @GetMapping("/register") String register(Model model) {
        if (!model.containsAttribute("registration")) model.addAttribute("registration",new Registration("","",""));
        return "register";
    }

    @PostMapping("/register")
    String register(@Valid @ModelAttribute("registration") Registration registration,
        BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) return "register";
        try { users.register(registration.name(),registration.email(),registration.password()); }
        catch (IllegalArgumentException ex) {
            errors.rejectValue("email","duplicate",ex.getMessage());
            return "register";
        }
        redirect.addFlashAttribute("registered",true);
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    String dashboard(Authentication authentication,Model model) {
        User user=users.current(authentication.getName());
        if (users.isAdmin(user)) return "redirect:/admin/dashboard";
        model.addAttribute("name",user.name);
        return "dashboard";
    }

    @GetMapping("/profile")
    String profile(Authentication authentication, Model model) {
        User user = users.current(authentication.getName());
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", new ProfileForm(user.name, user.email));
        }
        addProfileModel(model, user);
        return "profile";
    }

    @PostMapping("/profile")
    String updateProfile(Authentication authentication,
                         @Valid @ModelAttribute("profileForm") ProfileForm form,
                         BindingResult errors,
                         Model model,
                         HttpServletRequest request) {
        User current = users.current(authentication.getName());
        if (errors.hasErrors()) {
            addProfileModel(model, current);
            return "profile";
        }

        User updated;
        try {
            updated = users.updateProfile(authentication.getName(), form.name(), form.email());
        } catch (IllegalArgumentException ex) {
            errors.rejectValue("email", "duplicate", ex.getMessage());
            addProfileModel(model, current);
            return "profile";
        }

        if (!authentication.getName().equalsIgnoreCase(updated.email)) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            return "redirect:/login?profileUpdated";
        }
        return "redirect:/profile?updated";
    }

    private void addProfileModel(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", users.isAdmin(user));
    }
}
