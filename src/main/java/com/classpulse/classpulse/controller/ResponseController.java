package com.classpulse.classpulse.controller;

import com.classpulse.classpulse.dto.request.SubmitResponseRequest;
import com.classpulse.classpulse.dto.response.ResponseResponse;
import com.classpulse.classpulse.entity.Response;
import com.classpulse.classpulse.service.ResponseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/responses")
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    // GET /api/responses - Get all responses (TEACHER only - to see all responses)
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ResponseResponse>> getAllResponses() {
        List<ResponseResponse> responses = responseService.getAllResponses().stream()
                .map(ResponseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // GET /api/responses/{id} - Get response by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseResponse> getResponseById(@PathVariable Long id) {
        ResponseResponse response = ResponseResponse.fromEntity(responseService.getResponseById(id));
        return ResponseEntity.ok(response);
    }

    // GET /api/responses/question/{questionId} - Get responses by question
    // (paginated)
    @GetMapping("/question/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Page<ResponseResponse>> getResponsesByQuestion(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ResponseResponse> responses = responseService.getResponsesByQuestion(questionId, pageable)
                .map(ResponseResponse::fromEntity);
        return ResponseEntity.ok(responses);
    }

    // GET /api/responses/user/{userId} - Get responses by user (own responses)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResponseResponse>> getResponsesByUser(@PathVariable Long userId) {
        List<ResponseResponse> responses = responseService.getResponsesByUser(userId).stream()
                .map(ResponseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // POST /api/responses - Submit a response (STUDENT only)
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseResponse> submitResponse(@Valid @RequestBody SubmitResponseRequest request) {
        Response response = responseService.submitResponse(
                request.getQuestionId(),
                request.getUserId(),
                request.getAnswer());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseResponse.fromEntity(response));
    }

    // PUT /api/responses/{id} - Update response (STUDENT only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseResponse> updateResponse(@PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String answer = request.get("answer");
        Response response = responseService.updateResponse(id, answer);
        return ResponseEntity.ok(ResponseResponse.fromEntity(response));
    }

    // DELETE /api/responses/{id} - Delete response (TEACHER can delete any, STUDENT
    // own)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        responseService.deleteResponse(id);
        return ResponseEntity.noContent().build();
    }
}
