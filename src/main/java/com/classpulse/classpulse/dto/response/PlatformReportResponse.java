package com.classpulse.classpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PlatformReportResponse {

    // Overall stats
    private long totalUsers;
    private long totalTeachers;
    private long totalStudents;
    private long totalSessions;
    private long activeSessions;
    private long totalQuestions;
    private long totalResponses;

    // Top performers
    private List<TeacherStats> topTeachers;
    private List<SessionStats> recentSessions;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class TeacherStats {
        private Long teacherId;
        private String teacherName;
        private long sessionCount;
        private long totalResponses;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class SessionStats {
        private Long sessionId;
        private String sessionTitle;
        private String teacherName;
        private String status;
        private int questionCount;
        private int responseCount;
    }
}
