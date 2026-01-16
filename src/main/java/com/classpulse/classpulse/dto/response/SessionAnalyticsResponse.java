package com.classpulse.classpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SessionAnalyticsResponse {

    private Long sessionId;
    private String sessionTitle;
    private String sessionStatus;

    // Participation stats
    private int totalQuestions;
    private int totalResponses;
    private int uniqueParticipants;
    private double averageResponsesPerQuestion;

    // Question-wise breakdown
    private List<QuestionAnalytics> questionAnalytics;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class QuestionAnalytics {
        private Long questionId;
        private String questionText;
        private String questionType;
        private int responseCount;
        private Map<String, Long> answerDistribution; // For MCQ: answer -> count
        private Double averageRating; // For RATING type
    }
}
