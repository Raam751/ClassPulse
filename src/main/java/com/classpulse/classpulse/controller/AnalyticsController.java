package com.classpulse.classpulse.controller;

import com.classpulse.classpulse.dto.response.SessionAnalyticsResponse;
import com.classpulse.classpulse.dto.response.TeacherDashboardResponse;
import com.classpulse.classpulse.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Analytics and reporting APIs")
@PreAuthorize("hasRole('TEACHER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Get session analytics", description = "Get detailed participation stats for a session including question-wise breakdown")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SessionAnalyticsResponse> getSessionAnalytics(@PathVariable Long sessionId) {
        SessionAnalyticsResponse analytics = analyticsService.getSessionAnalytics(sessionId);
        return ResponseEntity.ok(analytics);
    }

    @Operation(summary = "Get teacher dashboard", description = "Get overall stats for a teacher including all sessions and engagement metrics")
    @GetMapping("/teacher/{teacherId}/dashboard")
    public ResponseEntity<TeacherDashboardResponse> getTeacherDashboard(@PathVariable Long teacherId) {
        TeacherDashboardResponse dashboard = analyticsService.getTeacherDashboard(teacherId);
        return ResponseEntity.ok(dashboard);
    }
}
