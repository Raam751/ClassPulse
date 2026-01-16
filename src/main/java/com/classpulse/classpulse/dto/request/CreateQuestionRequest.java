package com.classpulse.classpulse.dto.request;

import com.classpulse.classpulse.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// validations 
@Getter
@Setter
public class CreateQuestionRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "Question text is required")
    private String text;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    // Optional - only required for MCQ type
    private String optionsJson;
}
