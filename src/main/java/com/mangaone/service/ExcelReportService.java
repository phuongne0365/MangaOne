package com.mangaone.service;

import java.time.LocalDate;

public interface ExcelReportService {
    byte[] generateReport(LocalDate startDate, LocalDate endDate, String reporterName);
}
