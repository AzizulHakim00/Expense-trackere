package com.expensetracker.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "users")
public class User {
    @Id private String id;
    private String fullName;
    @Indexed(unique = true) private String email;
    private String password;
    private String role = "USER";
    public User() {}
    public User(String fullName, String email, String password) { this.fullName=fullName; this.email=email; this.password=password; }
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;}
}