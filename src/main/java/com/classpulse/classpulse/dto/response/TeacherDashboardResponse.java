package com.classpulse.classpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TeacherDashboardResponse {

    private Long teacherId;
    private String teacherName;

    // Session stats
    private int totalSessions;
    private int activeSessions;
    private int endedSessions;

    // Engagement stats
    private int totalQuestions;
    private int totalResponses;
    private int totalUniqueStudents;

    // Averages
    private double averageResponsesPerSession;
    private double averageQuestionsPerSession;
}
