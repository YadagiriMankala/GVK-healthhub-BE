package com.gvk.healthhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for:
 *   POST /POZAppServices/investigationcontrol/tests               (list)
 *   GET  /POZAppServices/investigationcontrol/packagetestdetails  (detail)
 *
 * Field names exactly match the REAL production API JSON response.
 * Verified against: https://api-gvk.healthsignz.com/POZAppServices/investigationcontrol/tests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosticTestDTO {

    // ---- Pricing (top-level, same values as order price details) ----
    private Double grossPrice;
    private Double discountPrice;
    private Double netPrice;
    private Integer quantity;
    private Double originalAmount;
    private Double otherDiscountAmount;
    private Double taxationAmount;
    private Double finalAmount;
    private Integer discountType;

    // ---- Service identity ----
    private Long serviceId;
    private String serviceName;
    private String slugName;
    private String tags;

    // ---- Parent category info ----
    private Long parentServiceId;
    private String parentServiceName;
    private Integer categoryId;
    private String categoryName;

    // ---- Department (sub-category) info ----
    private Long departmentId1;
    private Long departmentId2;
    private String departmentName1;
    private String departmentName2;

    // ---- Pricing details (home vs walk-in) ----
    private OrderPriceDetails homeOrderPriceDetails;
    private OrderPriceDetails walkinOrderPriceDetails;

    // ---- Logistics ----
    private Long pocId;
    private Long scheduleId;
    private Integer scheduleType;
    private Long cityId;
    private Long doctorId;

    /**
     * homeCollections: 0 = walk-in only, 1 = home collection available
     * Matches real API (integer, not boolean).
     */
    private Integer homeCollections;

    private Long expiryDate;

    // ---- Optional fields ----
    private String precaution;
    private String metaTitle;
    private String metaDescription;
    private Object vacutainerList;

    /** Sub-tests in a package/profile */
    private List<DiagnosticTestDTO> subServiceList;

    // ─── Nested: Order Price Details ───────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderPriceDetails {
        private Double grossPrice;
        private Double discountPrice;
        private Object taxes;           // null in current data
        private Double netPrice;
        private Integer type;           // 0 = standard
        private Object dayBasedPricing; // null in current data
        private Object specialPriceList;// null in current data
    }
}
