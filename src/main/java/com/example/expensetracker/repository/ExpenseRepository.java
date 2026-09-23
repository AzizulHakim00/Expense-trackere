package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    Optional<Expense> findByIdAndOwnerId(String id, String ownerId);
    List<Expense> findByOwnerIdAndDateGreaterThanEqualAndDateLessThanOrderByDateDescCreatedAtDesc(
        String ownerId, LocalDate start, LocalDate end);
}
