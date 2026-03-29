package com.financeapp.controller;

import com.financeapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/pdf/{userId}")
    public ResponseEntity<byte[]> pdf(@PathVariable Long userId) throws IOException {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.setContentDisposition(ContentDisposition.attachment()
                .filename("budget_report.pdf").build());
        return new ResponseEntity<>(reportService.exportPDF(userId), h, HttpStatus.OK);
    }

    @GetMapping("/csv/{userId}")
    public ResponseEntity<byte[]> csv(@PathVariable Long userId) throws IOException {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("text/csv"));
        h.setContentDisposition(ContentDisposition.attachment()
                .filename("budget_report.csv").build());
        return new ResponseEntity<>(reportService.exportCSV(userId), h, HttpStatus.OK);
    }
}