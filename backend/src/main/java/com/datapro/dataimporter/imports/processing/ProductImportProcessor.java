package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.catalog.domain.Product;
import com.datapro.dataimporter.catalog.repository.ProductRepository;
import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;
import com.datapro.dataimporter.imports.processing.Support.AbstractImportProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ProductImportProcessor extends AbstractImportProcessor<Product> {

    private static final Set<String> EXPECTED_HEADERS = Set.of("sku", "name", "price", "active");

    private final ProductRepository productRepository;

    public ProductImportProcessor(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    protected Set<String> expectedHeaders() {
        return EXPECTED_HEADERS;
    }

    @Override
    protected Optional<Product> parseRow(int rowNumber, Map<String, String> row, List<ProcessingError> errors, Set<String> seenKeys) {
        String sku = requiredValue(rowNumber, row, "sku", errors);
        String name = requiredValue(rowNumber, row, "name", errors);
        String priceValue = requiredValue(rowNumber, row, "price", errors);
        String activeValue = requiredValue(rowNumber, row, "active", errors);

        if (sku != null) {
            registerDuplicateIfNeeded(rowNumber, "sku", sku, seenKeys, errors);
            if (productRepository.existsBySku(sku)) {
                errors.add(new ProcessingError(
                        rowNumber,
                        "sku",
                        sku,
                        ImportErrorType.DUPLICATE_VALUE,
                        ImportErrorSeverity.CRITICAL,
                        "Ya existe un producto con ese SKU"
                ));
            }
        }

        BigDecimal price = priceValue == null ? null : parseDecimal(rowNumber, "price", priceValue, errors);
        Boolean active = parseBoolean(rowNumber, "active", activeValue, errors);

        if (!errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Product(sku, name, price, active));
    }

    @Override
    protected void persistValidRecords(List<Product> validRecords) {
        productRepository.saveAll(validRecords);
    }

    @Override
    protected ImportEntityType entityType() {
        return ImportEntityType.PRODUCT;
    }

    private Boolean parseBoolean(int rowNumber, String fieldName, String value, List<ProcessingError> errors) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        errors.add(new ProcessingError(
                rowNumber,
                fieldName,
                value,
                ImportErrorType.INVALID_FORMAT,
                ImportErrorSeverity.ERROR,
                "El valor booleano debe ser true o false"
        ));
        return null;
    }
}

