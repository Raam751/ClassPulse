package com.classpulse.classpulse.service;

import com.classpulse.classpulse.dto.response.SessionAnalyticsResponse;
import com.classpulse.classpulse.dto.response.SessionAnalyticsResponse.QuestionAnalytics;
import com.classpulse.classpulse.dto.response.TeacherDashboardResponse;
import com.classpulse.classpulse.entity.*;
import com.classpulse.classpulse.exception.ResourceNotFoundException;
import com.classpulse.classpulse.repository.QuestionRepository;
import com.classpulse.classpulse.repository.ResponseRepository;
import com.classpulse.classpulse.repository.SessionRepository;
import com.classpulse.classpulse.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final SessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final ResponseRepository responseRepository;
    private final UserRepository userRepository;

    public AnalyticsService(SessionRepository sessionRepository,
            QuestionRepository questionRepository,
            ResponseRepository responseRepository,
            UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get detailed analytics for a specific session
     */
    public SessionAnalyticsResponse getSessionAnalytics(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        List<Question> questions = questionRepository.findBySessionId(sessionId);

        // Collect all responses for this session's questions
        List<Response> allResponses = new ArrayList<>();
        List<QuestionAnalytics> questionAnalyticsList = new ArrayList<>();

        for (Question question : questions) {
            List<Response> responses = responseRepository.findByQuestionId(question.getId());
            allResponses.addAll(responses);

            // Build question analytics
            QuestionAnalytics qa = buildQuestionAnalytics(question, responses);
            questionAnalyticsList.add(qa);
        }

        // Calculate unique participants
        Set<Long> uniqueUserIds = allResponses.stream()
                .map(r -> r.getUser().getId())
                .collect(Collectors.toSet());

        double avgResponsesPerQuestion = questions.isEmpty() ? 0 : (double) allResponses.size() / questions.size();

        return SessionAnalyticsResponse.builder()
                .sessionId(session.getId())
                .sessionTitle(session.getTitle())
                .sessionStatus(session.getStatus().name())
                .totalQuestions(questions.size())
                .totalResponses(allResponses.size())
                .uniqueParticipants(uniqueUserIds.size())
                .averageResponsesPerQuestion(Math.round(avgResponsesPerQuestion * 100.0) / 100.0)
                .questionAnalytics(questionAnalyticsList)
                .build();
    }

    /**
     * Get teacher dashboard with overall stats
     */
    public TeacherDashboardResponse getTeacherDashboard(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        List<Session> sessions = sessionRepository.findByCreatedBy(teacher);

        int activeSessions = (int) sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .count();
        int endedSessions = (int) sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ENDED)
                .count();

        // Collect all questions and responses
        int totalQuestions = 0;
        int totalResponses = 0;
        Set<Long> uniqueStudents = new HashSet<>();

        for (Session session : sessions) {
            List<Question> questions = questionRepository.findBySessionId(session.getId());
            totalQuestions += questions.size();

            for (Question question : questions) {
                List<Response> responses = responseRepository.findByQuestionId(question.getId());
                totalResponses += responses.size();
                responses.forEach(r -> uniqueStudents.add(r.getUser().getId()));
            }
        }

        double avgResponsesPerSession = sessions.isEmpty() ? 0 : (double) totalResponses / sessions.size();
        double avgQuestionsPerSession = sessions.isEmpty() ? 0 : (double) totalQuestions / sessions.size();

        return TeacherDashboardResponse.builder()
                .teacherId(teacher.getId())
                .teacherName(teacher.getName())
                .totalSessions(sessions.size())
                .activeSessions(activeSessions)
                .endedSessions(endedSessions)
                .totalQuestions(totalQuestions)
                .totalResponses(totalResponses)
                .totalUniqueStudents(uniqueStudents.size())
                .averageResponsesPerSession(Math.round(avgResponsesPerSession * 100.0) / 100.0)
                .averageQuestionsPerSession(Math.round(avgQuestionsPerSession * 100.0) / 100.0)
                .build();
    }

    /**
     * Build analytics for a single question
     */
    private QuestionAnalytics buildQuestionAnalytics(Question question, List<Response> responses) {
        Map<String, Long> answerDistribution = responses.stream()
                .collect(Collectors.groupingBy(Response::getAnswer, Collectors.counting()));

        Double averageRating = null;
        if (question.getType() == QuestionType.RATING) {
            averageRating = responses.stream()
                    .mapToDouble(r -> {
                        try {
                            return Double.parseDouble(r.getAnswer());
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .average()
                    .orElse(0);
            averageRating = Math.round(averageRating * 100.0) / 100.0;
        }

        return QuestionAnalytics.builder()
                .questionId(question.getId())
                .questionText(question.getText())
                .questionType(question.getType().name())
                .responseCount(responses.size())
                .answerDistribution(answerDistribution)
                .averageRating(averageRating)
                .build();
    }
}
