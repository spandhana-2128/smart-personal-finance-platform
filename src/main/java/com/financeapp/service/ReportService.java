package com.financeapp.service;

import com.financeapp.model.Budget;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BudgetService budgetService;

    public byte[] exportPDF(Long userId) throws IOException {
        List<Budget> budgets = budgetService.getBudgetsByUser(userId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        Document doc = new Document(new PdfDocument(writer));

        doc.add(new Paragraph("Budget Report - User #" + userId)
                .setFontSize(18).setBold()
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph(" "));

        Table table = new Table(new float[]{2, 2, 2, 2, 2});
        for (String h : new String[]{"Month", "Year", "Limit", "Spent", "Status"})
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold()));

        for (Budget b : budgets) {
            double pct = b.getSpendingPercentage();
            table.addCell(b.getMonth());
            table.addCell(String.valueOf(b.getYear()));
            table.addCell(String.format("%.2f", b.getMonthlyLimit()));
            table.addCell(String.format("%.2f", b.getCurrentSpending()));
            table.addCell(pct >= 100 ? "EXCEEDED" : pct >= 70 ? "WARNING" : "OK");
        }

        doc.add(table);
        doc.close();
        return baos.toByteArray();
    }

    public byte[] exportCSV(Long userId) throws IOException {
        List<Budget> budgets = budgetService.getBudgetsByUser(userId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CSVPrinter printer = new CSVPrinter(
                new PrintWriter(new OutputStreamWriter(baos)),
                CSVFormat.DEFAULT.withHeader("Month", "Year", "Limit",
                        "Spent", "Strategy", "Status"));

        for (Budget b : budgets) {
            double pct = b.getSpendingPercentage();
            printer.printRecord(b.getMonth(), b.getYear(),
                    String.format("%.2f", b.getMonthlyLimit()),
                    String.format("%.2f", b.getCurrentSpending()),
                    b.getStrategyType(),
                    pct >= 100 ? "EXCEEDED" : pct >= 70 ? "WARNING" : "OK");
        }
        printer.flush();
        printer.close();
        return baos.toByteArray();
    }
}