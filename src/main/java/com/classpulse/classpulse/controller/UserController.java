package com.classpulse.classpulse.controller;

import com.classpulse.classpulse.dto.request.CreateUserRequest;
import com.classpulse.classpulse.dto.response.UserResponse;
import com.classpulse.classpulse.entity.User;
import com.classpulse.classpulse.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users - Get all users (TEACHER only - for admin purposes)
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // GET /api/users/{id} - Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = UserResponse.fromEntity(userService.getUserById(id));
        return ResponseEntity.ok(user);
    }

    // GET /api/users/teachers - Get all teachers
    @GetMapping("/teachers")
    public ResponseEntity<List<UserResponse>> getAllTeachers() {
        List<UserResponse> teachers = userService.getAllTeachers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teachers);
    }

    // GET /api/users/students - Get all students (TEACHER only)
    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserResponse>> getAllStudents() {
        List<UserResponse> students = userService.getAllStudents().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    // POST /api/users - Create new user (Use /api/auth/register instead)
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(createdUser));
    }

    // PUT /api/users/{id} - Update user
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    // DELETE /api/users/{id} - Delete user (TEACHER only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
