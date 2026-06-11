package com.gvk.healthhub.service;

import com.gvk.healthhub.entity.DiagnosticCategory;
import com.gvk.healthhub.entity.DiagnosticTest;
import com.gvk.healthhub.repository.DiagnosticCategoryRepository;
import com.gvk.healthhub.repository.DiagnosticTestRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DiagnosticExcelRowProcessor {

    private final DiagnosticTestRepository testRepository;
    private final DiagnosticCategoryRepository categoryRepository;

    @Autowired
    public DiagnosticExcelRowProcessor(DiagnosticTestRepository testRepository,
                                       DiagnosticCategoryRepository categoryRepository) {
        this.testRepository = testRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Processes a single Excel row inside its own isolated transaction boundary.
     * If this method throws an exception, only this row's changes are rolled back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String processRow(Row row, DataFormatter formatter, int rowNum) {
        // 1. Read fields
        Long serviceId = getLongValue(row.getCell(0), formatter);
        String slugName = getStringValue(row.getCell(1), formatter);
        String serviceName = getStringValue(row.getCell(2), formatter);
        String tags = getStringValue(row.getCell(3), formatter);
        Long parentServiceId = getLongValue(row.getCell(4), formatter);
        String parentServiceName = getStringValue(row.getCell(5), formatter);
        Integer categoryId = getIntegerValue(row.getCell(6), formatter);
        String categoryName = getStringValue(row.getCell(7), formatter);
        Long departmentId1 = getLongValue(row.getCell(8), formatter);
        Long departmentId2 = getLongValue(row.getCell(9), formatter);
        String departmentName1 = getStringValue(row.getCell(10), formatter);
        String departmentName2 = getStringValue(row.getCell(11), formatter);

        BigDecimal grossPrice = getBigDecimalValue(row.getCell(12), formatter);
        BigDecimal discountPrice = getBigDecimalValue(row.getCell(13), formatter);
        BigDecimal netPrice = getBigDecimalValue(row.getCell(14), formatter);
        BigDecimal originalAmount = getBigDecimalValue(row.getCell(15), formatter);
        BigDecimal finalAmount = getBigDecimalValue(row.getCell(16), formatter);
        BigDecimal taxationAmount = getBigDecimalValue(row.getCell(17), formatter);
        BigDecimal otherDiscountAmount = getBigDecimalValue(row.getCell(18), formatter);
        Integer discountType = getIntegerValue(row.getCell(19), formatter);

        Long brandId = getLongValue(row.getCell(20), formatter);
        Long pocId = getLongValue(row.getCell(21), formatter);
        Long scheduleId = getLongValue(row.getCell(22), formatter);
        Integer scheduleType = getIntegerValue(row.getCell(23), formatter);
        Long cityId = getLongValue(row.getCell(24), formatter);
        Long doctorId = getLongValue(row.getCell(25), formatter);
        String pinCode = getStringValue(row.getCell(26), formatter);
        Integer homeCollections = getIntegerValue(row.getCell(27), formatter);
        Long expiryDate = getLongValue(row.getCell(28), formatter);
        String precaution = getStringValue(row.getCell(29), formatter);
        String metaTitle = getStringValue(row.getCell(30), formatter);
        String metaDescription = getStringValue(row.getCell(31), formatter);
        Boolean isPopular = getBooleanValue(row.getCell(32), formatter);
        Boolean isActive = getBooleanValue(row.getCell(33), formatter);
        String categoryLookupStr = getStringValue(row.getCell(34), formatter);

        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service Name is required.");
        }

        // 2. Fetch or Create Diagnostic Test
        DiagnosticTest test;
        boolean isUpdate = false;
        if (serviceId != null) {
            test = testRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Service ID " + serviceId + " does not exist. Leave the Service ID cell empty to create a new test."));
            isUpdate = true;
        } else {
            test = new DiagnosticTest();
        }

        // Update fields
        test.setServiceName(serviceName);
        test.setTags(tags != null ? tags : serviceName);
        test.setParentServiceId(parentServiceId);
        test.setParentServiceName(parentServiceName);
        test.setCategoryId(categoryId);
        test.setCategoryName(categoryName);
        test.setDepartmentId1(departmentId1);
        test.setDepartmentId2(departmentId2);
        test.setDepartmentName1(departmentName1);
        test.setDepartmentName2(departmentName2);

        test.setGrossPrice(grossPrice);
        test.setDiscountPrice(discountPrice);
        test.setNetPrice(netPrice);
        test.setOriginalAmount(originalAmount != null ? originalAmount : BigDecimal.ZERO);
        test.setFinalAmount(finalAmount != null ? finalAmount : BigDecimal.ZERO);
        test.setTaxationAmount(taxationAmount != null ? taxationAmount : BigDecimal.ZERO);
        test.setOtherDiscountAmount(otherDiscountAmount != null ? otherDiscountAmount : BigDecimal.ZERO);
        test.setDiscountType(discountType != null ? discountType : 0);

        test.setBrandId(brandId);
        test.setPocId(pocId);
        test.setScheduleId(scheduleId);
        test.setScheduleType(scheduleType != null ? scheduleType : 2);
        test.setCityId(cityId);
        test.setDoctorId(doctorId);
        test.setPinCode(pinCode);
        test.setHomeCollections(homeCollections != null ? homeCollections : 0);
        test.setExpiryDate(expiryDate);
        test.setPrecaution(precaution);
        test.setMetaTitle(metaTitle);
        test.setMetaDescription(metaDescription);
        test.setIsPopular(isPopular != null ? isPopular : false);
        test.setIsActive(isActive != null ? isActive : true);

        // 3. Resolve slugName
        if (slugName != null && !slugName.trim().isEmpty()) {
            test.setSlugName(slugName.trim().toLowerCase());
        } else if (!isUpdate || test.getSlugName() == null) {
            String baseSlug = serviceName
                    .toLowerCase()
                    .replaceAll("[^a-z0-9\\-]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
            if (baseSlug.isEmpty()) {
                baseSlug = "diagnostic-test";
            }
            String finalSlug = baseSlug;
            int count = 1;
            while (testRepository.existsBySlugName(finalSlug)) {
                finalSlug = baseSlug + "-" + count++;
            }
            test.setSlugName(finalSlug);
        }

        // 4. Resolve Category relation
        if (categoryLookupStr != null) {
            try {
                Long catId = null;
                if (categoryLookupStr.contains(" - ")) {
                    catId = Long.parseLong(categoryLookupStr.split(" - ")[0].trim());
                } else {
                    catId = Long.parseLong(categoryLookupStr.trim());
                }
                Optional<DiagnosticCategory> catOpt = categoryRepository.findById(catId);
                if (catOpt.isPresent()) {
                    test.setDiagnosticCategory(catOpt.get());
                } else {
                    test.setDiagnosticCategory(null);
                }
            } catch (Exception e) {
                test.setDiagnosticCategory(null);
            }
        } else {
            test.setDiagnosticCategory(null);
        }

        testRepository.saveAndFlush(test);

        return "Row " + rowNum + ": " + (isUpdate ? "Updated" : "Created") + " test \"" 
                + test.getServiceName() + "\" (Service ID: " + test.getServiceId() + ")";
    }

    private String getStringValue(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        String val = formatter.formatCellValue(cell).trim();
        return val.isEmpty() ? null : val;
    }

    private Integer getIntegerValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return (int) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long getLongValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return (long) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal getBigDecimalValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean getBooleanValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        return "TRUE".equalsIgnoreCase(val) || "YES".equalsIgnoreCase(val) || "Y".equalsIgnoreCase(val);
    }
}
