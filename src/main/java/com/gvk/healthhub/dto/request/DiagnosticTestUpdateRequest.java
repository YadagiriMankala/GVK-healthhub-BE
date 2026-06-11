package com.gvk.healthhub.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for PATCH /investigationcontrol/tests/{serviceId}
 *
 * Supports updating a diagnostic test (lab test or health checkup package).
 * All fields are optional — only non-null fields are applied (PATCH semantics).
 *
 * Updatable fields:
 *  - Pricing       : grossPrice, discountPrice, netPrice
 *  - Info          : serviceName, slugName, tags, precaution
 *  - Category      : categoryId, categoryName, diagnosticCategoryId
 *  - Status        : isActive, isPopular, homeCollections
 *  - SEO           : metaTitle, metaDescription
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DiagnosticTestUpdateRequest {

    // ---- Basic Info ----
    private String serviceName;
    private String slugName;
    private String tags;
    private String precaution;

    // ---- Pricing ----
    private BigDecimal grossPrice;
    private BigDecimal discountPrice;
    private BigDecimal netPrice;

    // ---- Category (used for changing lab test → package or vice-versa) ----
    /**
     * categoryId:
     *   1 = Radiology
     *   2 = nILab
     *   3 = Laboratory Investigations (Pathology)
     *   4 = Package (Health Checkup)
     *   5 = Profile
     */
    private Integer categoryId;
    private String categoryName;

    /**
     * diagnosticCategoryId: FK to t_diagnostic_category
     *  e.g. 30012=Serology, 40001=Full Body Checkup, 40002=Diabetes Care
     */
    private Long diagnosticCategoryId;

    // ---- Status ----
    private Boolean isActive;
    private Boolean isPopular;

    /**
     * homeCollections:
     *   0 = walk-in only
     *   1 = home collection available
     */
    private Integer homeCollections;

    // ---- SEO ----
    private String metaTitle;
    private String metaDescription;
}
