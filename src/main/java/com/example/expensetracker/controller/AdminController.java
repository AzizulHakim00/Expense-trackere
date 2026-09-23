package com.example.expensetracker.controller;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import com.example.expensetracker.service.UserService.AdminUserRow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService users;

    public AdminController(UserService users) {
        this.users = users;
    }

    public record UserEditForm(
        @NotBlank @Size(max=80) String name,
        @NotBlank @Email @Size(max=160) String email,
        @NotNull Role role
    ) {}

    @GetMapping("/dashboard")
    String dashboard(Authentication authentication, Model model) {
        User admin = users.current(authentication.getName());
        List<AdminUserRow> rows = users.adminRows();
        model.addAttribute("name", admin.name);
        model.addAttribute("stats", users.adminStats());
        model.addAttribute("recentUsers", rows.stream().limit(5).toList());
        return "admin-dashboard";
    }

    @GetMapping("/users")
    String manageUsers(Authentication authentication,
                       @RequestParam(defaultValue="") String q,
                       @RequestParam(required=false) Role role,
                       @RequestParam(defaultValue="") String status,
                       Model model) {
        String query = q.trim().toLowerCase(Locale.ROOT);
        List<AdminUserRow> rows = users.adminRows().stream()
            .filter(row -> query.isBlank()
                || row.name().toLowerCase(Locale.ROOT).contains(query)
                || row.email().toLowerCase(Locale.ROOT).contains(query))
            .filter(row -> role == null || row.role() == role)
            .filter(row -> status.isBlank()
                || ("active".equalsIgnoreCase(status) && row.enabled())
                || ("disabled".equalsIgnoreCase(status) && !row.enabled()))
            .toList();

        model.addAttribute("rows", rows);
        model.addAttribute("q", q);
        model.addAttribute("selectedRole", role);
        model.addAttribute("status", status);
        model.addAttribute("currentUserId", users.current(authentication.getName()).id);
        model.addAttribute("roles", Role.values());
        return "admin-users";
    }

    @GetMapping("/users/{id}")
    String editUser(Authentication authentication, @PathVariable String id, Model model) {
        User target = users.byId(id);
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserEditForm(
                target.name,
                target.email,
                target.role == null ? Role.USER : target.role
            ));
        }
        addEditModel(authentication, model, target);
        return "admin-user";
    }

    @PostMapping("/users/{id}")
    String updateUser(Authentication authentication,
                      @PathVariable String id,
                      @Valid @ModelAttribute("userForm") UserEditForm form,
                      BindingResult errors,
                      Model model,
                      RedirectAttributes redirect) {
        User target = users.byId(id);
        if (errors.hasErrors()) {
            addEditModel(authentication, model, target);
            return "admin-user";
        }
        try {
            users.adminUpdate(authentication.getName(), id, form.name(), form.email(), form.role());
        } catch (IllegalArgumentException ex) {
            errors.reject("adminUpdate", ex.getMessage());
            addEditModel(authentication, model, target);
            return "admin-user";
        }
        redirect.addFlashAttribute("adminSuccess", "User profile updated.");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/status")
    String toggleStatus(Authentication authentication,
                        @PathVariable String id,
                        RedirectAttributes redirect) {
        try {
            users.toggleEnabled(authentication.getName(), id);
            redirect.addFlashAttribute("adminSuccess", "Account status updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("adminError", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    String deleteUser(Authentication authentication,
                      @PathVariable String id,
                      RedirectAttributes redirect) {
        try {
            users.deleteUser(authentication.getName(), id);
            redirect.addFlashAttribute("adminSuccess", "User and owned expenses deleted.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("adminError", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void addEditModel(Authentication authentication, Model model, User target) {
        User actor = users.current(authentication.getName());
        model.addAttribute("target", target);
        model.addAttribute("roles", Arrays.asList(Role.values()));
        model.addAttribute("expenseCount", users.expenseCount(target.id));
        model.addAttribute("isSelf", actor.id.equals(target.id));
        model.addAttribute("isDemoAdmin", UserService.DEMO_ADMIN_EMAIL.equals(target.email));
    }
}
