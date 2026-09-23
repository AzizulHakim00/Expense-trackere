package com.example.expensetracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
    @NotBlank @Size(max=100) String title,
    @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal amount,
    @NotBlank @Size(max=40) String category,
    @NotNull LocalDate date,
    @Size(max=500) String note
) {}
