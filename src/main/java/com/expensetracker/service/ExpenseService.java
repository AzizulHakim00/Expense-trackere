package com.expensetracker.service;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
@Service
public class ExpenseService {
    private final ExpenseRepository repo;
    public ExpenseService(ExpenseRepository repo){this.repo=repo;}
    public List<Expense> all(String uid){return repo.findByUserIdOrderByExpenseDateDesc(uid);}
    public Expense getOwned(String uid,String id){
        Expense e=repo.findById(id).orElseThrow(()->new IllegalArgumentException("Expense not found."));
        if(!uid.equals(e.getUserId())) throw new IllegalArgumentException("Expense not found.");
        return e;
    }
    public Expense create(String uid, ExpenseRequest r){Expense e=new Expense();e.setUserId(uid);apply(e,r);return repo.save(e);}
    public Expense update(String uid,String id,ExpenseRequest r){Expense e=getOwned(uid,id);apply(e,r);return repo.save(e);}
    public void delete(String uid,String id){repo.delete(getOwned(uid,id));}
    private void apply(Expense e,ExpenseRequest r){
        if(r.getTitle()==null||r.getTitle().trim().isEmpty()) throw new IllegalArgumentException("Title is required.");
        if(r.getAmount()==null||r.getAmount().compareTo(BigDecimal.ZERO)<=0) throw new IllegalArgumentException("Amount must be greater than 0.");
        e.setTitle(r.getTitle().trim()); e.setAmount(r.getAmount());
        e.setCategory(r.getCategory()==null||r.getCategory().isBlank()?"Other":r.getCategory().trim());
        e.setExpenseDate(r.getExpenseDate()==null?LocalDate.now():r.getExpenseDate());
        e.setNote(r.getNote()==null?"":r.getNote().trim());
    }
    public List<Expense> weekly(String uid, LocalDate ref){LocalDate s=ref.with(DayOfWeek.MONDAY);return repo.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(uid,s,s.plusDays(6));}
    public List<Expense> monthly(String uid, LocalDate ref){LocalDate s=ref.with(TemporalAdjusters.firstDayOfMonth());return repo.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(uid,s,s.with(TemporalAdjusters.lastDayOfMonth()));}
    public BigDecimal total(List<Expense> xs){return xs.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);}
}