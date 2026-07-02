package com.datapro.dataimporter.catalog.repository;

import com.datapro.dataimporter.catalog.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}
