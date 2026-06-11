package com.gvk.healthhub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DiagnosticTest entity — represents an individual diagnostic test or package.
 *
 * Fields verified against REAL production API response:
 *   POST https://api-gvk.healthsignz.com/POZAppServices/investigationcontrol/tests
 *
 * Sample fields from production:
 *   serviceId, serviceName, slugName, tags,
 *   parentServiceId, parentServiceName, categoryId, categoryName,
 *   departmentId1, departmentId2, departmentName1, departmentName2,
 *   grossPrice, discountPrice, netPrice, homeCollections (int 0/1),
 *   pocId, scheduleId, scheduleType, cityId, doctorId, expiryDate,
 *   precaution, metaTitle, metaDescription, vacutainerList
 */
@Entity
@Table(name = "t_diagnostic_test")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "slug_name", length = 255, unique = true)
    private String slugName;

    @Column(name = "service_name", length = 500, nullable = false)
    private String serviceName;

    /** Free-text search tags (same as serviceName in prod) */
    @Column(name = "tags", length = 500)
    private String tags;

    // ---- Parent (top-level) service/category ----

    /** parentServiceId from real API (e.g. 3 = "Laboratory Investigations") */
    @Column(name = "parent_service_id")
    private Long parentServiceId;

    @Column(name = "parent_service_name", length = 255)
    private String parentServiceName;

    /**
     * categoryId from real API:
     *   1=Radiology, 2=nILab, 3=Laboratory Investigations,
     *   4=Package, 5=Profile
     */
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name", length = 255)
    private String categoryName;

    // ---- Department (sub-category) ----

    /** departmentId1 — broad department (e.g. 1001 = "Tests") */
    @Column(name = "department_id1")
    private Long departmentId1;

    /** departmentId2 — specific department (e.g. 30013 = "Clinical Biochemistry") */
    @Column(name = "department_id2")
    private Long departmentId2;

    @Column(name = "department_name1", length = 255)
    private String departmentName1;

    @Column(name = "department_name2", length = 255)
    private String departmentName2;

    // ---- Pricing ----

    @Column(name = "gross_price", precision = 10, scale = 2)
    private BigDecimal grossPrice;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "net_price", precision = 10, scale = 2)
    private BigDecimal netPrice;

    @Column(name = "original_amount", precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "taxation_amount", precision = 10, scale = 2)
    private BigDecimal taxationAmount;

    @Column(name = "other_discount_amount", precision = 10, scale = 2)
    private BigDecimal otherDiscountAmount;

    @Column(name = "discount_type")
    private Integer discountType;

    // ---- Logistics ----

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "poc_id")
    private Long pocId;

    @Column(name = "schedule_id")
    private Long scheduleId;

    /** scheduleType: 2 = standard (from real API) */
    @Column(name = "schedule_type")
    private Integer scheduleType;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "pin_code", length = 20)
    private String pinCode;

    /**
     * homeCollections:
     *   0 = walk-in only (no homeOrderPriceDetails in API)
     *   1 = home collection available (has homeOrderPriceDetails)
     * Integer to match exact API response format.
     */
    @Column(name = "home_collections")
    private Integer homeCollections;

    /** Epoch ms — e.g. 1793471400000 */
    @Column(name = "expiry_date")
    private Long expiryDate;

    // ---- Optional / SEO fields ----

    @Column(name = "precaution", columnDefinition = "TEXT")
    private String precaution;

    @Column(name = "meta_title", length = 500)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    // ---- Status ----

    @Column(name = "is_popular")
    @Builder.Default
    private Boolean isPopular = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // ---- Relationships ----

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_category_id")
    private DiagnosticCategory diagnosticCategory;

    /** Sub-tests / included tests (for packages/profiles) */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_service_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Builder.Default
    private List<DiagnosticTest> subServiceList = new ArrayList<>();
}
