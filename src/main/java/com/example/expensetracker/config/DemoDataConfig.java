package com.example.expensetracker.config;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoDataConfig {
    public static final String ADMIN_EMAIL = "admin@expense.local";
    public static final String ADMIN_PASSWORD = "Admin123!";
    public static final String USER_EMAIL = "user@expense.local";
    public static final String USER_PASSWORD = "User123!";

    @Bean
    ApplicationRunner seedDemoData(
        UserRepository userRepository,
        ExpenseRepository expenseRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            User admin = upsertDemoUser(
                userRepository, passwordEncoder,
                "Demo Admin", ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN
            );
            User user = upsertDemoUser(
                userRepository, passwordEncoder,
                "Demo User", USER_EMAIL, USER_PASSWORD, Role.USER
            );

            seedCurrentMonthExpenses(expenseRepository, admin);
            seedCurrentMonthExpenses(expenseRepository, user);
        };
    }

    private User upsertDemoUser(
        UserRepository repository,
        PasswordEncoder encoder,
        String name,
        String email,
        String password,
        Role role
    ) {
        User user = repository.findByEmail(email).orElseGet(User::new);
        user.name = name;
        user.email = email;
        user.passwordHash = encoder.encode(password);
        user.role = role;
        return repository.save(user);
    }

    private void seedCurrentMonthExpenses(ExpenseRepository repository, User owner) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = YearMonth.from(today).atDay(1);
        LocalDate monthEnd = monthStart.plusMonths(1);

        if (!repository
            .findByOwnerIdAndDateGreaterThanEqualAndDateLessThanOrderByDateDescCreatedAtDesc(
                owner.id, monthStart, monthEnd)
            .isEmpty()) {
            return;
        }

        int availableDays = today.getDayOfMonth() - 1;
        LocalDate d1 = today;
        LocalDate d2 = today.minusDays(Math.min(1, availableDays));
        LocalDate d3 = today.minusDays(Math.min(2, availableDays));
        LocalDate d4 = today.minusDays(Math.min(4, availableDays));
        LocalDate d5 = today.minusDays(Math.min(6, availableDays));

        repository.saveAll(List.of(
            expense(owner.id, "Groceries", "62.50", "Food", d1, "Weekly grocery shopping"),
            expense(owner.id, "Bus / fuel", "18.00", "Transport", d2, "Daily transport"),
            expense(owner.id, "Lunch", "14.75", "Food", d3, "Lunch expense"),
            expense(owner.id, "Internet bill", "35.00", "Utilities", d4, "Monthly internet"),
            expense(owner.id, "Books", "28.50", "Education", d5, "Study materials"),
            expense(owner.id, "Rent", "420.00", "Housing", monthStart, "Monthly rent")
        ));
    }

    private Expense expense(
        String ownerId,
        String title,
        String amount,
        String category,
        LocalDate date,
        String note
    ) {
        Instant now = Instant.now();
        Expense expense = new Expense();
        expense.ownerId = ownerId;
        expense.title = title;
        expense.amount = new BigDecimal(amount);
        expense.category = category;
        expense.date = date;
        expense.note = note;
        expense.createdAt = now;
        expense.updatedAt = now;
        return expense;
    }
}
