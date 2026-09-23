package com.example.expensetracker.service;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ExpenseService {
    private final ExpenseRepository expenses;
    public ExpenseService(ExpenseRepository expenses) { this.expenses = expenses; }

    public List<ExpenseResponse> list(String ownerId, LocalDate start, LocalDate end) {
        if (start == null || end == null || !start.isBefore(end) || start.plusYears(5).isBefore(end))
            throw new IllegalArgumentException("Choose a valid date range shorter than five years");
        return expenses.findOwnedInDateRange(ownerId,start,end)
            .stream().map(this::map).toList();
    }

    public ExpenseResponse get(String ownerId, String id) { return map(owned(ownerId, id)); }

    @CacheEvict(value="summaries", allEntries=true)
    public ExpenseResponse create(String ownerId, ExpenseRequest request) {
        Expense expense = new Expense(); expense.ownerId = ownerId; expense.createdAt = Instant.now();
        apply(expense, request); return map(expenses.save(expense));
    }

    @CacheEvict(value="summaries", allEntries=true)
    public ExpenseResponse update(String ownerId, String id, ExpenseRequest request) {
        Expense expense = owned(ownerId,id); apply(expense, request); return map(expenses.save(expense));
    }

    @CacheEvict(value="summaries", allEntries=true)
    public void delete(String ownerId, String id) { expenses.delete(owned(ownerId,id)); }

    @Cacheable(value="summaries", key="#ownerId + ':month:' + #month")
    public SummaryResponse month(String ownerId, YearMonth month) {
        LocalDate start = month.atDay(1); return summarize(ownerId,start,start.plusMonths(1));
    }

    @Cacheable(value="summaries", key="#ownerId + ':week:' + #date")
    public SummaryResponse week(String ownerId, LocalDate date) {
        LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return summarize(ownerId,start,start.plusWeeks(1));
    }

    private SummaryResponse summarize(String ownerId, LocalDate start, LocalDate end) {
        List<ExpenseResponse> rows = list(ownerId,start,end);
        BigDecimal total = BigDecimal.ZERO; Map<String, BigDecimal> categories = new TreeMap<>();
        for (ExpenseResponse row : rows) {
            total = total.add(row.amount());
            categories.merge(row.category(),row.amount(),BigDecimal::add);
        }
        return new SummaryResponse(start,end,total,rows.size(),categories);
    }

    private Expense owned(String ownerId, String id) {
        return expenses.findByIdAndOwnerId(id,ownerId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Expense not found"));
    }
    private void apply(Expense e, ExpenseRequest r) {
        e.title=r.title().trim(); e.amount=r.amount(); e.category=r.category().trim();
        e.date=r.date(); e.note=r.note() == null ? "" : r.note().trim(); e.updatedAt=Instant.now();
    }
    private ExpenseResponse map(Expense e) {
        return new ExpenseResponse(e.id,e.title,e.amount,e.category,e.date,e.note,e.createdAt,e.updatedAt);
    }
}
