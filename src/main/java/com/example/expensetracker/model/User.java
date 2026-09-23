package com.example.expensetracker.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public class User {
    @Id public String id;
    public String name;
    @Indexed(unique = true) public String email;
    public String passwordHash;
    public Role role;
    public Boolean enabled = true;
    public Instant createdAt;
    public Instant updatedAt;

    public User() {}

    public User(String name, String email, String passwordHash) {
        this(name, email, passwordHash, Role.USER);
    }

    public User(String name, String email, String passwordHash, Role role) {
        Instant now = Instant.now();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = true;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public boolean isEnabledAccount() {
        return enabled == null || enabled;
    }
}
