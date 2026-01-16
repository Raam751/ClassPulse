# ClassPulse Learning Notes

A collection of concepts and explanations from building this project.

---

## DTO Layer - How It Works

### The Data Flow (GET Requests - Censoring Data)

```
Database → Entity (all fields) → Response DTO (safe fields only) → Client
```

**Key Point:** Response DTOs simply don't include sensitive fields. The `fromEntity()` method only copies safe fields.

```java
// Entity has password
public class User {
    private String password;  // ⚠️ Exists here
}

// Response DTO excludes it
public class UserResponse {
    // ❌ No password field - it's simply not here!
    
    public static UserResponse fromEntity(User user) {
        // password is never copied
    }
}
```

---

### Request vs Response DTOs

| Type | Used For | Purpose | Annotations |
|------|----------|---------|-------------|
| **Request DTOs** | POST/PUT (incoming) | **Validate** input | `@NotBlank`, `@Email`, `@Size` |
| **Response DTOs** | GET (outgoing) | **Censor/Filter** output | None (just exclude fields) |

---

### Where CreateQuestionRequest is Used

In `QuestionController.java`:

```java
@PostMapping
public ResponseEntity<QuestionResponse> createQuestion(
        @Valid @RequestBody CreateQuestionRequest request) {
    // Spring: JSON → DTO object
    // @Valid: triggers validation
    // If validation fails → 400 Bad Request
    // If passes → controller uses request.getSessionId(), etc.
}
```

**Flow:**
1. Client sends JSON
2. Spring maps JSON → `CreateQuestionRequest` object
3. `@Valid` checks `@NotNull`, `@NotBlank`, etc.
4. Validation fails → 400 error
5. Validation passes → Controller receives validated data

---

## Project Structure

```
com.classpulse.classpulse/
├── entity/          ← JPA entities (DB models, has ALL fields)
├── dto/
│   ├── request/     ← Validation for incoming data (POST/PUT)
│   └── response/    ← Safe output for outgoing data (GET)
├── repository/      ← Spring Data JPA interfaces
├── service/         ← Business logic
├── controller/      ← REST endpoints
└── config/          ← Configuration (Security, WebSocket later)
```

---

## Enums Used

| Enum | Values | Purpose |
|------|--------|---------|
| `Role` | TEACHER, STUDENT | User type |
| `SessionStatus` | CREATED, ACTIVE, ENDED | Session lifecycle |
| `QuestionType` | MCQ, RATING, OPEN_TEXT | Question types |

---

## @Transactional - What It Does

### The Problem
Without transactions, if a method fails midway, partial changes are saved to the database, leaving data inconsistent.

### The Solution
`@Transactional` wraps the method in a database transaction:
- **All operations succeed → COMMIT** (changes saved)
- **Any exception → ROLLBACK** (all changes undone)

```java
@Transactional
public void createSession(...) {
    // Transaction STARTS
    userRepo.findById(teacherId);   // Read
    sessionRepo.save(session);       // Write (not committed yet)
    // Transaction COMMITS only if no exception
    // If exception: ROLLBACK (session not saved)
}
```

### readOnly = true
```java
@Transactional(readOnly = true)
public List<User> getAllUsers() { ... }
```
- Optimizes performance (skips dirty checking)
- Can route to read replicas in scaled systems
- Makes intent clear: this method doesn't modify data

---

*More notes will be added as we build...*
