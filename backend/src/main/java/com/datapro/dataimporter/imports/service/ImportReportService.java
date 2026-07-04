package com.datapro.dataimporter.imports.service;

import com.datapro.dataimporter.imports.domain.ReportFormat;
import com.datapro.dataimporter.imports.dto.ImportJobFilterCriteria;
import com.datapro.dataimporter.imports.dto.ImportJobSummaryResponse;
import com.datapro.dataimporter.imports.dto.ReportFileResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ImportReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ImportJobService importJobService;

    public ImportReportService(ImportJobService importJobService) {
        this.importJobService = importJobService;
    }

    @Transactional(readOnly = true)
    public ReportFileResponse export(ReportFormat format, ImportJobFilterCriteria criteria) {
        List<ImportJobSummaryResponse> rows = importJobService.findForReport(criteria);

        return switch (format) {
            case CSV -> new ReportFileResponse(
                    buildCsv(rows),
                    "import-report.csv",
                    "text/csv"
            );
            case PDF -> new ReportFileResponse(
                    buildPdf(rows),
                    "import-report.pdf",
                    "application/pdf"
            );
        };
    }

    private byte[] buildCsv(List<ImportJobSummaryResponse> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("id,file_name,entity_type,file_type,import_mode,status,total_records,successful_records,failed_records,success_rate,duration_ms,initiated_by,created_at\n");

        for (ImportJobSummaryResponse row : rows) {
            builder.append(row.id()).append(',')
                    .append(escapeCsv(row.fileName())).append(',')
                    .append(row.entityType()).append(',')
                    .append(row.fileType()).append(',')
                    .append(row.importMode()).append(',')
                    .append(row.status()).append(',')
                    .append(row.totalRecords()).append(',')
                    .append(row.successfulRecords()).append(',')
                    .append(row.failedRecords()).append(',')
                    .append(String.format(java.util.Locale.US, "%.2f", row.successRate())).append(',')
                    .append(row.durationMs()).append(',')
                    .append(escapeCsv(row.initiatedByName())).append(',')
                    .append(row.createdAt().format(DATE_FORMATTER))
                    .append('\n');
        }

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildPdf(List<ImportJobSummaryResponse> rows) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float y = 790;
                contentStream.beginText();
                contentStream.setFont(titleFont, 16);
                contentStream.newLineAtOffset(40, y);
                contentStream.showText("Data Importer Pro - Import Report");
                contentStream.endText();

                y -= 30;
                contentStream.beginText();
                contentStream.setFont(bodyFont, 10);
                contentStream.newLineAtOffset(40, y);
                contentStream.showText("Total rows: " + rows.size());
                contentStream.endText();

                y -= 25;
                for (ImportJobSummaryResponse row : rows) {
                    if (y < 70) {
                        break;
                    }

                    String line = String.format(
                            java.util.Locale.US,
                            "#%d %s | %s | %s | ok=%d fail=%d | %.2f%% | %s",
                            row.id(),
                            truncate(row.fileName(), 22),
                            row.entityType(),
                            row.status(),
                            row.successfulRecords(),
                            row.failedRecords(),
                            row.successRate(),
                            row.createdAt().format(DATE_FORMATTER)
                    );

                    contentStream.beginText();
                    contentStream.setFont(bodyFont, 9);
                    contentStream.newLineAtOffset(40, y);
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= 16;
                }
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible generar el reporte PDF", ex);
        }
    }

    private String escapeCsv(String value) {
        String sanitized = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + sanitized + "\"";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}

