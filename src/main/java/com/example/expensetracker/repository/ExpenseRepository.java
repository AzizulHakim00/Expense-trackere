package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    Optional<Expense> findByIdAndOwnerId(String id, String ownerId);

    @Query(
        value = "{ 'ownerId': ?0, 'date': { '$gte': ?1, '$lt': ?2 } }",
        sort = "{ 'date': -1, 'createdAt': -1 }"
    )
    List<Expense> findOwnedInDateRange(String ownerId, LocalDate start, LocalDate end);
}
