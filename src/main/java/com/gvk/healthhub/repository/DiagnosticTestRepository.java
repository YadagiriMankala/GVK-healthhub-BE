package com.gvk.healthhub.repository;

import com.gvk.healthhub.entity.DiagnosticTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DiagnosticTest entity.
 *
 * Supports:
 *   POST /POZAppServices/investigationcontrol/tests         (paginated test list)
 *   GET  /POZAppServices/investigationcontrol/packagetestdetails (detail by slugName)
 *
 * Real POST body from production:
 *   { "brandId":55, "pocId":3717, "categoryId":0,
 *     "departmentIdList1":[], "departmentIdList2":[],
 *     "departmentIdList3":[], "departmentIdList4":[],
 *     "homeCollections":false, "searchTerm":"",
 *     "sortBy":"popular", "from":0, "size":50 }
 */
@Repository
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {

    /** Find by slug — used by packagetestdetails endpoint */
    Optional<DiagnosticTest> findBySlugNameAndIsActiveTrue(String slugName);

    /** Find by serviceId — secondary lookup for packagetestdetails */
    Optional<DiagnosticTest> findByServiceIdAndIsActiveTrue(Long serviceId);

    /** Check if slug exists */
    boolean existsBySlugName(String slugName);

    /**
     * Full-featured search matching the real POST /tests request body.
     *
     * Filter logic:
     *  - brandId        : exact match (skipped if 0 or null)
     *  - pocId          : exact match (skipped if 0 or null)
     *  - categoryId     : exact match (skipped if 0 or null)
     *  - homeCollections: 0=walk-in only, 1=home collection; null=both
     *  - searchTerm     : LIKE on serviceName and tags
     *  - departmentId1  : IN list filter on t.departmentId1 (broad dept)
     *  - departmentId2  : IN list filter on t.departmentId2 (specific dept)
     *  - serviceIdList  : restrict to specific service IDs (cart/bundle use case)
     *  - sortBy         : "popular" | "price_asc" | "price_desc"
     */
    @Query(value = """
            SELECT t FROM DiagnosticTest t
            WHERE t.isActive = true
              AND (:brandId   IS NULL OR :brandId   = 0 OR t.brandId   = :brandId)
              AND (:pocId     IS NULL OR :pocId     = 0 OR t.pocId     = :pocId)
              AND (:categoryId IS NULL OR :categoryId = 0 OR t.categoryId = :categoryId)
              AND (:homeCollections IS NULL OR t.homeCollections = :homeCollections)
              AND (
                    :searchTerm IS NULL OR :searchTerm = ''
                    OR LOWER(t.serviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(t.tags)        LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (:dept1Empty = true OR t.departmentId1 IN :departmentIdList1)
              AND (:dept2Empty = true OR t.departmentId2 IN :departmentIdList2)
              AND (:serviceIdsEmpty = true OR t.serviceId IN :serviceIdList)
              AND (:popularOnly = false OR t.isPopular = true)
            ORDER BY
              CASE WHEN :sortBy = 'popular' AND t.isPopular = true THEN 0 ELSE 1 END ASC,
              CASE WHEN :sortBy = 'price_asc'  THEN t.netPrice ELSE 0 END ASC,
              CASE WHEN :sortBy = 'price_desc' THEN t.netPrice ELSE 0 END DESC,
              t.serviceName ASC
            """,
            countQuery = """
            SELECT COUNT(t) FROM DiagnosticTest t
            WHERE t.isActive = true
              AND (:brandId   IS NULL OR :brandId   = 0 OR t.brandId   = :brandId)
              AND (:pocId     IS NULL OR :pocId     = 0 OR t.pocId     = :pocId)
              AND (:categoryId IS NULL OR :categoryId = 0 OR t.categoryId = :categoryId)
              AND (:homeCollections IS NULL OR t.homeCollections = :homeCollections)
              AND (
                    :searchTerm IS NULL OR :searchTerm = ''
                    OR LOWER(t.serviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(t.tags)        LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (:dept1Empty = true OR t.departmentId1 IN :departmentIdList1)
              AND (:dept2Empty = true OR t.departmentId2 IN :departmentIdList2)
              AND (:serviceIdsEmpty = true OR t.serviceId IN :serviceIdList)
              AND (:popularOnly = false OR t.isPopular = true)
            """)
    Page<DiagnosticTest> searchTests(
            @Param("brandId")          Long brandId,
            @Param("pocId")            Long pocId,
            @Param("categoryId")       Integer categoryId,
            @Param("homeCollections")  Integer homeCollections,
            @Param("searchTerm")       String searchTerm,
            @Param("departmentIdList1") List<Long> departmentIdList1,
            @Param("dept1Empty")       boolean dept1Empty,
            @Param("departmentIdList2") List<Long> departmentIdList2,
            @Param("dept2Empty")       boolean dept2Empty,
            @Param("serviceIdList")    List<Long> serviceIdList,
            @Param("serviceIdsEmpty")  boolean serviceIdsEmpty,
            @Param("popularOnly")      boolean popularOnly,
            @Param("sortBy")           String sortBy,
            Pageable pageable
    );
}
