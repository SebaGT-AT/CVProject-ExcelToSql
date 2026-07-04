package com.datapro.dataimporter.imports.controller;

import com.datapro.dataimporter.catalog.repository.CustomerRepository;
import com.datapro.dataimporter.catalog.repository.EmployeeRepository;
import com.datapro.dataimporter.catalog.repository.InventoryItemRepository;
import com.datapro.dataimporter.catalog.repository.ProductRepository;
import com.datapro.dataimporter.imports.repository.ImportJobRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportUploadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private ImportJobRepository importJobRepository;

    @BeforeEach
    void setUp() {
        importJobRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@datapro.com", roles = {"ADMIN"})
    void shouldImportValidProductsAndSkipInvalidOnPartialMode() throws Exception {
        String csv = """
                sku,name,price,active
                PRD-001,Laptop,1299.90,true
                PRD-002,Mouse,not-a-number,true
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                csv.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/import-jobs/upload")
                        .file(file)
                        .param("entityType", "PRODUCT")
                        .param("importMode", "PARTIAL_ALLOWED"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_COMPLETED"))
                .andExpect(jsonPath("$.data.successfulRecords").value(1))
                .andExpect(jsonPath("$.data.failedRecords").value(1))
                .andExpect(jsonPath("$.data.errors[0].errorType").value("INVALID_NUMBER"));

        org.assertj.core.api.Assertions.assertThat(productRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(productRepository.findBySku("PRD-001")).isPresent();
    }

    @Test
    @WithMockUser(username = "operator@datapro.com", roles = {"OPERATOR"})
    void shouldCancelFullImportWhenThereAreRowErrors() throws Exception {
        String csv = """
                customer_code,full_name,email,phone,registration_date
                CUST-001,Ana Perez,ana@example.com,+56911111111,2026-07-01
                CUST-002,Juan Soto,correo-invalido,+56922222222,2026-07-02
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.csv",
                "text/csv",
                csv.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/import-jobs/upload")
                        .file(file)
                        .param("entityType", "CUSTOMER")
                        .param("importMode", "FULL"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.successfulRecords").value(0))
                .andExpect(jsonPath("$.data.failedRecords").value(2));

        org.assertj.core.api.Assertions.assertThat(customerRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "admin@datapro.com", roles = {"ADMIN"})
    void shouldImportEmployeesFromXlsx() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildEmployeeWorkbook()
        );

        mockMvc.perform(multipart("/api/v1/import-jobs/upload")
                        .file(file)
                        .param("entityType", "EMPLOYEE")
                        .param("importMode", "PARTIAL_ALLOWED"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileType").value("XLSX"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.successfulRecords").value(2));

        org.assertj.core.api.Assertions.assertThat(employeeRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "admin@datapro.com", roles = {"ADMIN"})
    void shouldCancelInventoryImportWhenProductDoesNotExist() throws Exception {
        String csv = """
                product_sku,quantity,warehouse_location,last_updated
                SKU-NOT-FOUND,10,SCL-A,2026-07-02
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "inventory.csv",
                "text/csv",
                csv.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/import-jobs/upload")
                        .file(file)
                        .param("entityType", "INVENTORY")
                        .param("importMode", "CANCEL_ON_CRITICAL"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.failedRecords").value(1))
                .andExpect(jsonPath("$.data.errors[0].errorType").value("FOREIGN_KEY_NOT_FOUND"));

        org.assertj.core.api.Assertions.assertThat(inventoryItemRepository.count()).isZero();
    }

    private byte[] buildEmployeeWorkbook() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("employees");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("employee_code");
            header.createCell(1).setCellValue("full_name");
            header.createCell(2).setCellValue("email");
            header.createCell(3).setCellValue("phone");
            header.createCell(4).setCellValue("hire_date");

            var row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("EMP-001");
            row1.createCell(1).setCellValue("Laura Diaz");
            row1.createCell(2).setCellValue("laura@example.com");
            row1.createCell(3).setCellValue("+56977777777");
            row1.createCell(4).setCellValue("2026-06-30");

            var row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("EMP-002");
            row2.createCell(1).setCellValue("Pedro Mora");
            row2.createCell(2).setCellValue("pedro@example.com");
            row2.createCell(3).setCellValue("+56988888888");
            row2.createCell(4).setCellValue("2026-07-01");

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
