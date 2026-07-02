package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.common.exception.BusinessRuleException;
import com.datapro.dataimporter.imports.domain.ImportFileType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ImportFileReader {

    public ParsedImportFile read(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Debes seleccionar un archivo CSV o XLSX");
        }

        ImportFileType fileType = resolveFileType(file.getOriginalFilename());
        List<Map<String, String>> rows = switch (fileType) {
            case CSV -> readCsv(file);
            case XLSX -> readXlsx(file);
        };

        return new ParsedImportFile(fileType, rows);
    }

    private ImportFileType resolveFileType(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessRuleException("El archivo debe tener un nombre valido");
        }

        String normalized = originalFilename.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".csv")) {
            return ImportFileType.CSV;
        }
        if (normalized.endsWith(".xlsx")) {
            return ImportFileType.XLSX;
        }

        throw new BusinessRuleException("Solo se permiten archivos CSV o XLSX");
    }

    private List<Map<String, String>> readCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            List<String> headers = parser.getHeaderNames().stream().map(this::normalizeHeader).toList();
            List<Map<String, String>> rows = new ArrayList<>();

            parser.forEach(record -> {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), record.get(i).trim());
                }
                rows.add(row);
            });

            return rows;
        } catch (IOException ex) {
            throw new BusinessRuleException("No fue posible leer el archivo CSV");
        }
    }

    private List<Map<String, String>> readXlsx(MultipartFile file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return List.of();
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(normalizeHeader(formatter.formatCellValue(cell)));
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row rowSheet = sheet.getRow(rowIndex);
                if (rowSheet == null) {
                    continue;
                }

                Map<String, String> row = new LinkedHashMap<>();
                boolean hasAnyValue = false;
                for (int i = 0; i < headers.size(); i++) {
                    String value = formatter.formatCellValue(rowSheet.getCell(i));
                    if (!value.isBlank()) {
                        hasAnyValue = true;
                    }
                    row.put(headers.get(i), value.trim());
                }

                if (hasAnyValue) {
                    rows.add(row);
                }
            }

            return rows;
        } catch (IOException ex) {
            throw new BusinessRuleException("No fue posible leer el archivo XLSX");
        }
    }

    private String normalizeHeader(String rawHeader) {
        return rawHeader == null
                ? ""
                : rawHeader.trim().toLowerCase(Locale.ROOT).replace(" ", "_").replace("-", "_");
    }
}
