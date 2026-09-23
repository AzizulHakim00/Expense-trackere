package com.expensetracker.service;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Locale;
@Service
public class UserService {
    private final UserRepository repo; private final PasswordEncoder encoder;
    public UserService(UserRepository repo, PasswordEncoder encoder){this.repo=repo;this.encoder=encoder;}
    public User register(RegisterRequest r){
        String name=r.getFullName()==null?"":r.getFullName().trim();
        String email=r.getEmail()==null?"":r.getEmail().trim().toLowerCase(Locale.ROOT);
        if(name.length()<3) throw new IllegalArgumentException("Full name must be at least 3 characters.");
        if(!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new IllegalArgumentException("Enter a valid email.");
        if(r.getPassword()==null || r.getPassword().length()<6) throw new IllegalArgumentException("Password must be at least 6 characters.");
        if(!r.getPassword().equals(r.getConfirmPassword())) throw new IllegalArgumentException("Passwords do not match.");
        if(repo.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email already registered.");
        return repo.save(new User(name,email,encoder.encode(r.getPassword())));
    }
    public User current(Authentication a){return repo.findByEmailIgnoreCase(a.getName()).orElseThrow();}
}