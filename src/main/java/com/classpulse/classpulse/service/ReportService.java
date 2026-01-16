package com.classpulse.classpulse.service;

import com.classpulse.classpulse.dto.response.PlatformReportResponse;
import com.classpulse.classpulse.dto.response.PlatformReportResponse.SessionStats;
import com.classpulse.classpulse.dto.response.PlatformReportResponse.TeacherStats;
import com.classpulse.classpulse.entity.*;
import com.classpulse.classpulse.repository.QuestionRepository;
import com.classpulse.classpulse.repository.ResponseRepository;
import com.classpulse.classpulse.repository.SessionRepository;
import com.classpulse.classpulse.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final ResponseRepository responseRepository;

    public ReportService(UserRepository userRepository,
            SessionRepository sessionRepository,
            QuestionRepository questionRepository,
            ResponseRepository responseRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
    }

    /**
     * Generate a platform-wide report with aggregated statistics
     */
    public PlatformReportResponse generatePlatformReport() {
        // Basic counts
        long totalUsers = userRepository.count();
        long totalTeachers = userRepository.findByRole(Role.TEACHER).size();
        long totalStudents = userRepository.findByRole(Role.STUDENT).size();
        long totalSessions = sessionRepository.count();
        long activeSessions = sessionRepository.findByStatus(SessionStatus.ACTIVE).size();
        long totalQuestions = questionRepository.count();
        long totalResponses = responseRepository.count();

        // Get top teachers by session count
        List<TeacherStats> topTeachers = getTopTeachers(5);

        // Get recent sessions with stats
        List<SessionStats> recentSessions = getRecentSessionStats(10);

        return PlatformReportResponse.builder()
                .totalUsers(totalUsers)
                .totalTeachers(totalTeachers)
                .totalStudents(totalStudents)
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .totalQuestions(totalQuestions)
                .totalResponses(totalResponses)
                .topTeachers(topTeachers)
                .recentSessions(recentSessions)
                .build();
    }

    /**
     * Get top teachers by session count and engagement
     */
    private List<TeacherStats> getTopTeachers(int limit) {
        List<User> teachers = userRepository.findByRole(Role.TEACHER);

        return teachers.stream()
                .map(teacher -> {
                    List<Session> sessions = sessionRepository.findByCreatedBy(teacher);
                    long responseCount = sessions.stream()
                            .flatMap(s -> questionRepository.findBySessionId(s.getId()).stream())
                            .flatMap(q -> responseRepository.findByQuestionId(q.getId()).stream())
                            .count();

                    return TeacherStats.builder()
                            .teacherId(teacher.getId())
                            .teacherName(teacher.getName())
                            .sessionCount(sessions.size())
                            .totalResponses(responseCount)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getSessionCount(), a.getSessionCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get recent sessions with question and response counts
     */
    private List<SessionStats> getRecentSessionStats(int limit) {
        List<Session> sessions = sessionRepository.findAll(
                PageRequest.of(0, limit, Sort.by("createdAt").descending())).getContent();

        return sessions.stream()
                .map(session -> {
                    List<Question> questions = questionRepository.findBySessionId(session.getId());
                    int responseCount = questions.stream()
                            .mapToInt(q -> responseRepository.findByQuestionId(q.getId()).size())
                            .sum();

                    return SessionStats.builder()
                            .sessionId(session.getId())
                            .sessionTitle(session.getTitle())
                            .teacherName(session.getCreatedBy().getName())
                            .status(session.getStatus().name())
                            .questionCount(questions.size())
                            .responseCount(responseCount)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
