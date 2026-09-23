package com.expensetracker.repository;
import com.expensetracker.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;
public interface ExpenseRepository extends MongoRepository<Expense,String> {
    List<Expense> findByUserIdOrderByExpenseDateDesc(String userId);
    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(String userId, LocalDate start, LocalDate end);
}