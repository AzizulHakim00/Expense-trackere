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

    @Test void otherUsersCannotReadUpdateOrDeleteAnExpense() {
        when(repository.findByIdAndOwnerId("id", "user-b")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.get("user-b", "id"));
        assertThrows(ResponseStatusException.class, () -> service.update("user-b", "id",
            new ExpenseRequest("Lunch",new BigDecimal("12.50"),"Food", LocalDate.of(2026,9,23),"")));
        assertThrows(ResponseStatusException.class, () -> service.delete("user-b", "id"));
        verify(repository, never()).delete(any());
    }

    @Test void calendarMonthAndMondayWeekHaveCorrectExclusiveEndAndTotals() {
        Expense lunch = new Expense(); lunch.amount = new BigDecimal("12.50"); lunch.category = "Food";
        Expense train = new Expense(); train.amount = new BigDecimal("3.20"); train.category = "Transport";
        when(repository.findByOwnerIdAndDateGreaterThanEqualAndDateLessThanOrderByDateDescCreatedAtDesc(
            eq("user-a"),any(),any())).thenReturn(List.of(lunch,train));
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
