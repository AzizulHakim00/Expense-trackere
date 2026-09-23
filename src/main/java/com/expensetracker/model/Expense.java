package com.expensetracker.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;
@Document(collection = "expenses")
public class Expense {
    @Id private String id;
    @Indexed private String userId;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDate expenseDate;
    private String note;
    public Expense() {}
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public LocalDate getExpenseDate(){return expenseDate;} public void setExpenseDate(LocalDate v){expenseDate=v;}
    public String getNote(){return note;} public void setNote(String v){note=v;}
}