package com.datapro.dataimporter.imports.processing;

import com.datapro.dataimporter.catalog.domain.InventoryItem;
import com.datapro.dataimporter.catalog.repository.InventoryItemRepository;
import com.datapro.dataimporter.catalog.repository.ProductRepository;
import com.datapro.dataimporter.imports.domain.ImportEntityType;
import com.datapro.dataimporter.imports.domain.ImportErrorSeverity;
import com.datapro.dataimporter.imports.domain.ImportErrorType;
import com.datapro.dataimporter.imports.processing.Support.AbstractImportProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class InventoryImportProcessor extends AbstractImportProcessor<InventoryItem> {

    private static final Set<String> EXPECTED_HEADERS = Set.of("product_sku", "quantity", "warehouse_location", "last_updated");

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public InventoryImportProcessor(ProductRepository productRepository, InventoryItemRepository inventoryItemRepository) {
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    protected Set<String> expectedHeaders() {
        return EXPECTED_HEADERS;
    }

    @Override
    protected Optional<InventoryItem> parseRow(int rowNumber, Map<String, String> row, List<ProcessingError> errors, Set<String> seenKeys) {
        String productSku = requiredValue(rowNumber, row, "product_sku", errors);
        String quantityValue = requiredValue(rowNumber, row, "quantity", errors);
        String warehouseLocation = requiredValue(rowNumber, row, "warehouse_location", errors);
        String lastUpdatedValue = requiredValue(rowNumber, row, "last_updated", errors);

        Integer quantity = quantityValue == null ? null : parseInteger(rowNumber, "quantity", quantityValue, errors);
        LocalDate lastUpdated = lastUpdatedValue == null ? null : parseDate(rowNumber, "last_updated", lastUpdatedValue, errors);

        var product = productSku == null ? Optional.<com.datapro.dataimporter.catalog.domain.Product>empty() : productRepository.findBySku(productSku);
        if (productSku != null && product.isEmpty()) {
            errors.add(new ProcessingError(
                    rowNumber,
                    "product_sku",
                    productSku,
                    ImportErrorType.FOREIGN_KEY_NOT_FOUND,
                    ImportErrorSeverity.CRITICAL,
                    "No existe un producto con ese SKU"
            ));
        }

        if (quantity != null && quantity < 0) {
            errors.add(new ProcessingError(
                    rowNumber,
                    "quantity",
                    quantityValue,
                    ImportErrorType.INVALID_NUMBER,
                    ImportErrorSeverity.ERROR,
                    "La cantidad no puede ser negativa"
            ));
        }

        if (!errors.isEmpty() || product.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new InventoryItem(product.get(), quantity, warehouseLocation, lastUpdated));
    }

    @Override
    protected void persistValidRecords(List<InventoryItem> validRecords) {
        inventoryItemRepository.saveAll(validRecords);
    }

    @Override
    protected ImportEntityType entityType() {
        return ImportEntityType.INVENTORY;
    }
}

