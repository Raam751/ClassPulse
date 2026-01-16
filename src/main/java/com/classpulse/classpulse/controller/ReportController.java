package com.classpulse.classpulse.controller;

import com.classpulse.classpulse.dto.response.PlatformReportResponse;
import com.classpulse.classpulse.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Platform reports and complex queries")
@PreAuthorize("hasRole('TEACHER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Get platform report", description = "Get aggregated platform statistics including top teachers and recent sessions")
    @GetMapping("/platform")
    public ResponseEntity<PlatformReportResponse> getPlatformReport() {
        PlatformReportResponse report = reportService.generatePlatformReport();
        return ResponseEntity.ok(report);
    }
}
