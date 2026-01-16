package com.classpulse.classpulse.dto.response;

import com.classpulse.classpulse.entity.Question;
import com.classpulse.classpulse.entity.QuestionType;
import lombok.Getter;
import lombok.Setter;

// sensors
//  Controller converts Entity → DTO before sending response

@Getter
@Setter
public class QuestionResponse {

    private Long id;
    private Long sessionId;
    private String text;
    private QuestionType type;
    private String optionsJson;

    public static QuestionResponse fromEntity(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setSessionId(question.getSession().getId());
        response.setText(question.getText());
        response.setType(question.getType());
        response.setOptionsJson(question.getOptionsJson());
        return response;
    }
}
