package com.example.expensetracker.service;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    public static final String DEMO_ADMIN_EMAIL = "admin@expense.local";

    public record AdminStats(long totalUsers, long activeUsers, long totalExpenses, long expensesThisMonth) {}
    public record AdminUserRow(String id, String name, String email, Role role, boolean enabled,
                               long expenseCount, Instant createdAt) {}

    private final UserRepository users;
    private final ExpenseRepository expenses;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, ExpenseRepository expenses, PasswordEncoder encoder) {
        this.users = users;
        this.expenses = expenses;
        this.encoder = encoder;
    }

    public void register(String name, String email, String password) {
        String normalized = normalize(email);
        if (users.existsByEmail(normalized)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        try {
            users.save(new User(name.trim(), normalized, encoder.encode(password), Role.USER));
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }

    public User current(String email) {
        return users.findByEmail(normalize(email))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User byId(String id) {
        return users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User updateProfile(String currentEmail, String name, String email) {
        User user = current(currentEmail);
        String normalized = normalize(email);
        if (users.existsByEmailAndIdNot(normalized, user.id)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.name = name.trim();
        user.email = normalized;
        user.updatedAt = Instant.now();
        return users.save(user);
    }

    public List<AdminUserRow> adminRows() {
        return users.findAll().stream()
            .sorted(Comparator.comparing((User user) -> user.createdAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(user -> new AdminUserRow(
                user.id,
                user.name,
                user.email,
                user.role == null ? Role.USER : user.role,
                user.isEnabledAccount(),
                expenses.countByOwnerId(user.id),
                user.createdAt
            ))
            .toList();
    }

    public AdminStats adminStats() {
        List<User> all = users.findAll();
        long active = all.stream().filter(User::isEnabledAccount).count();
        LocalDate start = YearMonth.now().atDay(1);
        long monthExpenses = expenses.findAllInDateRange(start, start.plusMonths(1)).size();
        return new AdminStats(all.size(), active, expenses.count(), monthExpenses);
    }

    public User adminUpdate(String actorEmail, String targetId, String name, String email, Role role) {
        User actor = current(actorEmail);
        User target = byId(targetId);
        String normalized = normalize(email);

        if (users.existsByEmailAndIdNot(normalized, target.id)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (actor.id.equals(target.id) && !target.email.equals(normalized)) {
            throw new IllegalArgumentException("Use My Profile to change your own email");
        }
        if (actor.id.equals(target.id) && role != Role.ADMIN) {
            throw new IllegalArgumentException("You cannot remove your own administrator role");
        }
        if (DEMO_ADMIN_EMAIL.equals(target.email) && !DEMO_ADMIN_EMAIL.equals(normalized)) {
            throw new IllegalArgumentException("The demo administrator email cannot be changed");
        }
        if (DEMO_ADMIN_EMAIL.equals(target.email) && role != Role.ADMIN) {
            throw new IllegalArgumentException("The demo administrator must remain an ADMIN");
        }

        target.name = name.trim();
        target.email = normalized;
        target.role = role;
        target.updatedAt = Instant.now();
        return users.save(target);
    }

    public void toggleEnabled(String actorEmail, String targetId) {
        User actor = current(actorEmail);
        User target = byId(targetId);
        if (actor.id.equals(target.id)) {
            throw new IllegalArgumentException("You cannot disable your own account");
        }
        if (DEMO_ADMIN_EMAIL.equals(target.email)) {
            throw new IllegalArgumentException("The demo administrator cannot be disabled");
        }
        target.enabled = !target.isEnabledAccount();
        target.updatedAt = Instant.now();
        users.save(target);
    }

    public void deleteUser(String actorEmail, String targetId) {
        User actor = current(actorEmail);
        User target = byId(targetId);
        if (actor.id.equals(target.id)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        if (DEMO_ADMIN_EMAIL.equals(target.email)) {
            throw new IllegalArgumentException("The demo administrator cannot be deleted");
        }
        expenses.deleteByOwnerId(target.id);
        users.delete(target);
    }

    public long expenseCount(String userId) {
        return expenses.countByOwnerId(userId);
    }

    public boolean isAdmin(User user) {
        return user.role == Role.ADMIN;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = current(username);
        Role role = user.role == null ? Role.USER : user.role;
        return org.springframework.security.core.userdetails.User.withUsername(user.email)
            .password(user.passwordHash)
            .roles(role.name())
            .disabled(!user.isEnabledAccount())
            .build();
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
