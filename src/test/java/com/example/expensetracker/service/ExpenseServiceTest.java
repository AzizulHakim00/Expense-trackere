package com.example.expensetracker.service;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {
    private final ExpenseRepository repository = mock(ExpenseRepository.class);
    private final ExpenseService service = new ExpenseService(repository);

    @Test
    void createUpdateAndDeletePersistOwnedExpenses() {
        ExpenseRequest createRequest = new ExpenseRequest(
            "Groceries", new BigDecimal("25.50"), "Food",
            LocalDate.of(2026, 9, 23), "Market"
        );

        when(repository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            if (expense.id == null) {
                expense.id = "expense-1";
            }
            return expense;
        });

        var created = service.create("user-a", createRequest);
        assertEquals("expense-1", created.id());
        assertEquals("Groceries", created.title());
        assertEquals(new BigDecimal("25.50"), created.amount());

        Expense stored = new Expense();
        stored.id = "expense-1";
        stored.ownerId = "user-a";
        stored.title = "Groceries";
        stored.amount = new BigDecimal("25.50");
        stored.category = "Food";
        stored.date = LocalDate.of(2026, 9, 23);
        stored.note = "Market";

        when(repository.findByIdAndOwnerId("expense-1", "user-a"))
            .thenReturn(Optional.of(stored));

        ExpenseRequest updateRequest = new ExpenseRequest(
            "Groceries and fruit", new BigDecimal("31.25"), "Food",
            LocalDate.of(2026, 9, 23), "Updated"
        );

        var updated = service.update("user-a", "expense-1", updateRequest);
        assertEquals("Groceries and fruit", updated.title());
        assertEquals(new BigDecimal("31.25"), updated.amount());
        assertEquals("Updated", updated.note());

        service.delete("user-a", "expense-1");
        verify(repository).delete(stored);
        verify(repository, times(2)).save(any(Expense.class));
    }

    @Test
    void otherUsersCannotReadUpdateOrDeleteAnExpense() {
        when(repository.findByIdAndOwnerId("id", "user-b")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.get("user-b", "id"));
        assertThrows(ResponseStatusException.class, () -> service.update("user-b", "id",
            new ExpenseRequest("Lunch",new BigDecimal("12.50"),"Food", LocalDate.of(2026,9,23),"")));
        assertThrows(ResponseStatusException.class, () -> service.delete("user-b", "id"));
        verify(repository, never()).delete(any());
    }

    @Test
    void calendarMonthAndMondayWeekHaveCorrectExclusiveEndAndTotals() {
        Expense lunch = new Expense(); lunch.amount = new BigDecimal("12.50"); lunch.category = "Food";
        Expense train = new Expense(); train.amount = new BigDecimal("3.20"); train.category = "Transport";
        when(repository.findOwnedInDateRange(eq("user-a"),any(),any()))
            .thenReturn(List.of(lunch,train));

        var monthly = service.month("user-a",YearMonth.of(2026,9));
        assertEquals(LocalDate.of(2026,9,1),monthly.start());
        assertEquals(LocalDate.of(2026,10,1),monthly.endExclusive());
        assertEquals(new BigDecimal("15.70"),monthly.total());
        assertEquals(new BigDecimal("12.50"),monthly.byCategory().get("Food"));

        var weekly = service.week("user-a",LocalDate.of(2026,9,23));
        assertEquals(LocalDate.of(2026,9,21),weekly.start());
        assertEquals(LocalDate.of(2026,9,28),weekly.endExclusive());
    }
}
