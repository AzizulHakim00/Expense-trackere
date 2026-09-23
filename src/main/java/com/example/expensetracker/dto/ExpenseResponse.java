package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(String id, String title, BigDecimal amount, String category,
                              LocalDate date, String note, Instant createdAt, Instant updatedAt) {}
