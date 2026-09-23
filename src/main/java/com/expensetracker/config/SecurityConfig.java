package com.expensetracker.config;
import com.expensetracker.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
 @Bean UserDetailsService uds(UserRepository r){return email->r.findByEmailIgnoreCase(email).map(u->org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPassword()).roles(u.getRole()).build()).orElseThrow();}
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain chain(HttpSecurity http)throws Exception{return http.authorizeHttpRequests(a->a.requestMatchers("/","/login","/register","/css/**","/js/**").permitAll().anyRequest().authenticated()).formLogin(f->f.loginPage("/login").loginProcessingUrl("/login").usernameParameter("email").passwordParameter("password").defaultSuccessUrl("/dashboard",true).failureUrl("/login?error").permitAll()).logout(l->l.logoutSuccessUrl("/login?logout")).build();}
}