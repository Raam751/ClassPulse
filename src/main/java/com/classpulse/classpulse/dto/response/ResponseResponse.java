package com.classpulse.classpulse.dto.response;

import com.classpulse.classpulse.entity.Response;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseResponse {

    private Long id;
    private Long questionId;
    private Long userId;
    private String userName;
    private String answer;

    public static ResponseResponse fromEntity(Response response) {
        ResponseResponse dto = new ResponseResponse();
        dto.setId(response.getId());
        dto.setQuestionId(response.getQuestion().getId());
        dto.setUserId(response.getUser().getId());
        dto.setUserName(response.getUser().getName());
        dto.setAnswer(response.getAnswer());
        return dto;
    }
}
