package com.example.expensetracker.controller;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenses;
    private final UserService users;
    public ExpenseController(ExpenseService expenses,UserService users) { this.expenses=expenses;this.users=users; }
    private String owner(Authentication auth) { return users.current(auth.getName()).id; }

    @GetMapping List<ExpenseResponse> list(Authentication auth,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end) {
        return expenses.list(owner(auth),start,end);
    }
    @GetMapping("/{id}") ExpenseResponse get(Authentication auth,@PathVariable String id) {
        return expenses.get(owner(auth),id);
    }
    @PostMapping ResponseEntity<ExpenseResponse> create(Authentication auth,@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse created=expenses.create(owner(auth),request);
        return ResponseEntity.created(URI.create("/api/expenses/"+created.id())).body(created);
    }
    @PutMapping("/{id}") ExpenseResponse update(Authentication auth,@PathVariable String id,
                                                  @Valid @RequestBody ExpenseRequest request) {
        return expenses.update(owner(auth),id,request);
    }
    @DeleteMapping("/{id}") ResponseEntity<Void> delete(Authentication auth,@PathVariable String id) {
        expenses.delete(owner(auth),id);return ResponseEntity.noContent().build();
    }
    @GetMapping("/summary/monthly") SummaryResponse monthly(Authentication auth,
        @RequestParam @DateTimeFormat(pattern="yyyy-MM") YearMonth month) {
        return expenses.month(owner(auth),month);
    }
    @GetMapping("/summary/weekly") SummaryResponse weekly(Authentication auth,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date) {
        return expenses.week(owner(auth),date);
    }
}
