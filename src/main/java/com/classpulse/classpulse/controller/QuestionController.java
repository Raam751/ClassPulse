package com.classpulse.classpulse.controller;

import com.classpulse.classpulse.dto.request.CreateQuestionRequest;
import com.classpulse.classpulse.dto.response.QuestionResponse;
import com.classpulse.classpulse.entity.Question;
import com.classpulse.classpulse.entity.QuestionType;
import com.classpulse.classpulse.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // GET /api/questions - Get all questions (Any authenticated user)
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        List<QuestionResponse> questions = questionService.getAllQuestions().stream()
                .map(QuestionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }

    // GET /api/questions/{id} - Get question by ID
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long id) {
        QuestionResponse question = QuestionResponse.fromEntity(questionService.getQuestionById(id));
        return ResponseEntity.ok(question);
    }

    // GET /api/questions/session/{sessionId} - Get questions by session
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsBySession(@PathVariable Long sessionId) {
        List<QuestionResponse> questions = questionService.getQuestionsBySession(sessionId).stream()
                .map(QuestionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }

    // POST /api/questions - Create new question (TEACHER only)
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        Question question = questionService.createQuestion(
                request.getSessionId(),
                request.getText(),
                request.getType(),
                request.getOptionsJson());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionResponse.fromEntity(question));
    }

    // PUT /api/questions/{id} - Update question (TEACHER only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        QuestionType type = QuestionType.valueOf((String) request.get("type"));
        String optionsJson = (String) request.get("optionsJson");

        Question question = questionService.updateQuestion(id, text, type, optionsJson);
        return ResponseEntity.ok(QuestionResponse.fromEntity(question));
    }

    // DELETE /api/questions/{id} - Delete question (TEACHER only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
