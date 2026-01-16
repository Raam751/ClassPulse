package com.classpulse.classpulse.dto.response;

import com.classpulse.classpulse.entity.Session;
import com.classpulse.classpulse.entity.SessionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionResponse {

    private Long id;
    private String title;
    private String code;
    private SessionStatus status;
    private Long createdById;
    private String createdByName;

    public static SessionResponse fromEntity(Session session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setCode(session.getCode());
        response.setStatus(session.getStatus());
        response.setCreatedById(session.getCreatedBy().getId());
        response.setCreatedByName(session.getCreatedBy().getName());
        return response;
    }
}
