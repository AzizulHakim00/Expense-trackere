package com.example.expensetracker.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("expenses")
@CompoundIndex(name = "owner_date", def = "{'ownerId': 1, 'date': -1}")
public class Expense {
    @Id public String id;
    public String ownerId;
    public String title;
    public BigDecimal amount;
    public String category;
    public LocalDate date;
    public String note;
    public Instant createdAt;
    public Instant updatedAt;
}
