package com.datapro.dataimporter.imports.repository;

import com.datapro.dataimporter.imports.dto.ImportJobFilterCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public final class ImportJobSpecifications {

    private ImportJobSpecifications() {
    }

    public static Specification<com.datapro.dataimporter.imports.domain.ImportJob> byCriteria(ImportJobFilterCriteria criteria) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();

            if (criteria == null) {
                return builder.conjunction();
            }

            if (criteria.fileName() != null && !criteria.fileName().isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("fileName")),
                        "%" + criteria.fileName().trim().toLowerCase() + "%"
                ));
            }

            if (criteria.entityType() != null) {
                predicates.add(builder.equal(root.get("entityType"), criteria.entityType()));
            }

            if (criteria.status() != null) {
                predicates.add(builder.equal(root.get("status"), criteria.status()));
            }

            if (criteria.initiatedByEmail() != null && !criteria.initiatedByEmail().isBlank()) {
                var initiatedBy = root.join("initiatedBy");
                predicates.add(builder.equal(
                        builder.lower(initiatedBy.get("email")),
                        criteria.initiatedByEmail().trim().toLowerCase()
                ));
            }

            if (criteria.dateFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.dateFrom().atStartOfDay().atOffset(OffsetDateTime.now().getOffset())
                ));
            }

            if (criteria.dateTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.dateTo().atTime(LocalTime.MAX).atOffset(OffsetDateTime.now().getOffset())
                ));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
