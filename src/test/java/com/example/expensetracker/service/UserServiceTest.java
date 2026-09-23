package com.example.expensetracker.service;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UserService service = new UserService(userRepository, expenseRepository, encoder);

    @Test
    void disabledAccountIsDisabledInSecurityDetails() {
        User user = new User("Disabled User", "disabled@example.com", "hash", Role.USER);
        user.enabled = false;
        when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("disabled@example.com");

        assertFalse(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }

    @Test
    void adminCanUpdateToggleAndDeleteAnotherUser() {
        User admin = new User("Admin", "admin@example.com", "hash", Role.ADMIN);
        admin.id = "admin-id";
        User target = new User("User", "user@example.com", "hash", Role.USER);
        target.id = "user-id";

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById("user-id")).thenReturn(Optional.of(target));
        when(userRepository.existsByEmailAndIdNot("new@example.com", "user-id")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = service.adminUpdate(
            "admin@example.com", "user-id", "Updated User", "new@example.com", Role.ADMIN
        );
        assertEquals("Updated User", updated.name);
        assertEquals("new@example.com", updated.email);
        assertEquals(Role.ADMIN, updated.role);

        service.toggleEnabled("admin@example.com", "user-id");
        assertFalse(target.isEnabledAccount());

        service.deleteUser("admin@example.com", "user-id");
        verify(expenseRepository).deleteByOwnerId("user-id");
        verify(userRepository).delete(target);
    }

    @Test
    void adminCannotDisableOrDeleteSelf() {
        User admin = new User("Admin", "admin@example.com", "hash", Role.ADMIN);
        admin.id = "admin-id";
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById("admin-id")).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
            () -> service.toggleEnabled("admin@example.com", "admin-id"));
        assertThrows(IllegalArgumentException.class,
            () -> service.deleteUser("admin@example.com", "admin-id"));
    }
}
