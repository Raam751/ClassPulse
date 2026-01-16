package com.classpulse.classpulse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitResponseRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Answer is required")
    private String answer;
}
