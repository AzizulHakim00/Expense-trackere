package com.expensetracker.dto;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
public class ExpenseRequest {
    private String title, category, note;
    private BigDecimal amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate expenseDate;
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public LocalDate getExpenseDate(){return expenseDate;} public void setExpenseDate(LocalDate v){expenseDate=v;}
    public String getNote(){return note;} public void setNote(String v){note=v;}
}