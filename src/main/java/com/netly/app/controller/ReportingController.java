package com.netly.app.controller;

import com.netly.app.service.ReportingService;
import com.netly.app.service.ScheduledReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/portfolio")
    public ResponseEntity<String> sendPortfolioReport() {
        reportingService.sendReport();
        return ResponseEntity.ok("Portfolio report email queued successfully");
    }
}

