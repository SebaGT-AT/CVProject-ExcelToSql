package com.datapro.dataimporter.catalog.domain;

import com.datapro.dataimporter.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "inventory_items")
public class InventoryItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, length = 80)
    private String warehouseLocation;

    @Column(nullable = false)
    private LocalDate lastUpdated;

    protected InventoryItem() {
    }

    public InventoryItem(Product product, int quantity, String warehouseLocation, LocalDate lastUpdated) {
        this.product = product;
        this.quantity = quantity;
        this.warehouseLocation = warehouseLocation;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }
}

