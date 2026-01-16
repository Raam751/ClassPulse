package com.classpulse.classpulse.controller;

import com.classpulse.classpulse.dto.request.CreateSessionRequest;
import com.classpulse.classpulse.dto.response.SessionResponse;
import com.classpulse.classpulse.entity.Session;
import com.classpulse.classpulse.entity.SessionStatus;
import com.classpulse.classpulse.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // GET /api/sessions - Get all sessions (paginated) - Any authenticated user
    @GetMapping
    public ResponseEntity<Page<SessionResponse>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SessionResponse> sessions = sessionService.getAllSessions(pageable)
                .map(SessionResponse::fromEntity);
        return ResponseEntity.ok(sessions);
    }

    // GET /api/sessions/{id} - Get session by ID
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id) {
        SessionResponse session = SessionResponse.fromEntity(sessionService.getSessionById(id));
        return ResponseEntity.ok(session);
    }

    // GET /api/sessions/code/{code} - Get session by join code (PUBLIC - for
    // students to join)
    @GetMapping("/code/{code}")
    public ResponseEntity<SessionResponse> getSessionByCode(@PathVariable String code) {
        SessionResponse session = SessionResponse.fromEntity(sessionService.getSessionByCode(code));
        return ResponseEntity.ok(session);
    }

    // GET /api/sessions/teacher/{teacherId} - Get sessions by teacher
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByTeacher(@PathVariable Long teacherId) {
        List<SessionResponse> sessions = sessionService.getSessionsByTeacher(teacherId).stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    // GET /api/sessions/active - Get all active sessions
    @GetMapping("/active")
    public ResponseEntity<List<SessionResponse>> getActiveSessions() {
        List<SessionResponse> sessions = sessionService.getActiveSessions().stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    // GET /api/sessions/filter - Filter sessions with multiple criteria
    @GetMapping("/filter")
    @Operation(summary = "Filter sessions", description = "Filter sessions by status, teacher, and date range")
    public ResponseEntity<Page<SessionResponse>> filterSessions(
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SessionResponse> sessions = sessionService.filterSessions(status, teacherId, startDate, endDate, pageable)
                .map(SessionResponse::fromEntity);
        return ResponseEntity.ok(sessions);
    }

    // POST /api/sessions - Create new session (TEACHER only)
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        Session session = sessionService.createSession(request.getTitle(), request.getTeacherId());
        return ResponseEntity.status(HttpStatus.CREATED).body(SessionResponse.fromEntity(session));
    }

    // PUT /api/sessions/{id}/start - Start a session (TEACHER only)
    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SessionResponse> startSession(@PathVariable Long id) {
        SessionResponse session = SessionResponse.fromEntity(sessionService.startSession(id));
        return ResponseEntity.ok(session);
    }

    // PUT /api/sessions/{id}/end - End a session (TEACHER only)
    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SessionResponse> endSession(@PathVariable Long id) {
        SessionResponse session = SessionResponse.fromEntity(sessionService.endSession(id));
        return ResponseEntity.ok(session);
    }

    // DELETE /api/sessions/{id} - Delete session (TEACHER only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
