package com.gvk.healthhub.repository;

import com.gvk.healthhub.entity.DiagnosticCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DiagnosticCategory entity.
 *
 * Supports:
 *   GET /POZAppServices/investigationcontrol/getdiagnosticscategory
 *       ?brandId=55&homeCollections=false&pinCode=500033&categoryId=1001
 */
@Repository
public interface DiagnosticCategoryRepository extends JpaRepository<DiagnosticCategory, Long> {

    /**
     * Fetch all active TOP-LEVEL categories (parentCategory is null)
     * filtered by brandId, homeCollections flag, and optional categoryId.
     *
     * When categoryId = 0 (or 1001 as used in FE) treat it as "all categories".
     */
    @Query("""
            SELECT c FROM DiagnosticCategory c
            WHERE c.isActive = true
              AND c.parentCategory IS NULL
              AND (:brandId IS NULL OR c.brandId = :brandId)
              AND (:homeCollections IS NULL OR c.homeCollections = :homeCollections)
              AND (:categoryId = 0 OR :categoryId = 1001 OR c.categoryId = :categoryId)
            ORDER BY c.displayOrder ASC, c.categoryName ASC
            """)
    List<DiagnosticCategory> findTopLevelCategories(
            @Param("brandId") Long brandId,
            @Param("homeCollections") Boolean homeCollections,
            @Param("categoryId") Long categoryId
    );

    /** All active categories for a given brand (used for admin / seeding). */
    List<DiagnosticCategory> findByBrandIdAndIsActiveTrueOrderByDisplayOrderAsc(Long brandId);
}
