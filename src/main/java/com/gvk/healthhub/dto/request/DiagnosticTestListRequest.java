package com.gvk.healthhub.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for POST /POZAppServices/investigationcontrol/tests
 *
 * Field names exactly match the REAL production API request body.
 * Verified against live call:
 *   {"pinCode":"","from":0,"size":50,"brandId":55,
 *    "departmentIdList1":[],"departmentIdList2":[],
 *    "departmentIdList3":[],"departmentIdList4":[],
 *    "patientProfileId":0,"parentProfileId":0,
 *    "isWellness":false,"pocId":3717,"categoryId":0,
 *    "searchTerm":"","sortBy":"popular","homeCollections":false}
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiagnosticTestListRequest {

    /** Pin code of patient location (empty string = any) */
    private String pinCode;

    /** Pagination: start offset (0-based index, same as FE 'from') */
    private Integer from;

    /** Page size (default 50 in FE) */
    private Integer size;

    /** Brand ID, e.g. 55 for GVK */
    private Long brandId;

    /** POC (Point of Care) / hospital ID, e.g. 3717 */
    private Long pocId;

    /**
     * Category ID — 0 = all categories.
     * Maps to DiagnosticTest.categoryId in the entity.
     */
    private Integer categoryId;

    /**
     * Department filter lists — FE sends up to 4 arrays of department IDs.
     * departmentIdList1 → t.departmentId1 (broad, e.g. 1001 = "Tests")
     * departmentIdList2 → t.departmentId2 (specific, e.g. 30013 = "Clinical Biochemistry")
     * departmentIdList3 / 4 → reserved for future use
     */
    private List<Long> departmentIdList1;
    private List<Long> departmentIdList2;
    private List<Long> departmentIdList3;
    private List<Long> departmentIdList4;

    /** Patient profile ID (0 = not specified) */
    private Long patientProfileId;

    /** Parent profile ID (0 = not specified) */
    private Long parentProfileId;

    /** true = wellness/preventive tests only */
    private Boolean isWellness;

    /**
     * homeCollections: false = walk-in only, true = home collection available.
     * FE sends boolean; stored in entity as Integer (0/1).
     */
    private Boolean homeCollections;

    /** Full-text search term (empty string = no filter) */
    private String searchTerm;

    /**
     * Sort order:
     *   "popular"    → by isPopular DESC
     *   "price_asc"  → by netPrice ASC
     *   "price_desc" → by netPrice DESC
     */
    private String sortBy;

    /** Specific service IDs to fetch (used by cart/order flows) */
    private List<Long> serviceIdList;
}
