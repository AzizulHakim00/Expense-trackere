package com.example.expensetracker.service;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public void register(String name, String email, String password) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(normalized)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        try {
            users.save(new User(name.trim(), normalized, encoder.encode(password), Role.USER));
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }

    public User current(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return users.findByEmail(normalized)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = current(username);
        Role role = user.role == null ? Role.USER : user.role;
        return org.springframework.security.core.userdetails.User.withUsername(user.email)
            .password(user.passwordHash)
            .roles(role.name())
            .build();
    }
}
