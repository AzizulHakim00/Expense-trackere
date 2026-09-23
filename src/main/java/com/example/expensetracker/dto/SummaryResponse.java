package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record SummaryResponse(LocalDate start, LocalDate endExclusive, BigDecimal total,
                              int count, Map<String, BigDecimal> byCategory) {}
