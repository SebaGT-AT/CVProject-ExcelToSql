package com.datapro.dataimporter.imports.repository;

import com.datapro.dataimporter.imports.domain.ImportErrorType;
import com.datapro.dataimporter.imports.domain.ImportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    @Override
    @EntityGraph(attributePaths = {"initiatedBy"})
    Page<ImportJob> findAll(Pageable pageable);

    @Query("""
            select distinct ij
            from ImportJob ij
            left join fetch ij.errors
            left join fetch ij.initiatedBy
            where ij.id = :id
            """)
    Optional<ImportJob> findDetailedById(@Param("id") Long id);

    @Query("""
            select count(ij)
            from ImportJob ij
            where ij.createdAt between :start and :end
            """)
    long countCreatedBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("""
            select coalesce(sum(ij.totalRecords), 0)
            from ImportJob ij
            where ij.createdAt between :start and :end
            """)
    long sumTotalRecordsCreatedBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("""
            select coalesce(sum(ij.successfulRecords), 0)
            from ImportJob ij
            where ij.createdAt between :start and :end
            """)
    long sumSuccessfulRecordsCreatedBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("""
            select new com.datapro.dataimporter.imports.repository.ErrorTypeCount(ie.errorType, count(ie))
            from ImportError ie
            group by ie.errorType
            order by count(ie) desc
            """)
    List<ErrorTypeCount> findTopErrorTypes(Pageable pageable);

    @Query("""
            select ij
            from ImportJob ij
            order by ij.createdAt desc
            """)
    @EntityGraph(attributePaths = {"initiatedBy"})
    List<ImportJob> findRecentImports(Pageable pageable);
}
