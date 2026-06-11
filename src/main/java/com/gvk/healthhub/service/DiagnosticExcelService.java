package com.gvk.healthhub.service;

import com.gvk.healthhub.entity.DiagnosticCategory;
import com.gvk.healthhub.entity.DiagnosticTest;
import com.gvk.healthhub.repository.DiagnosticCategoryRepository;
import com.gvk.healthhub.repository.DiagnosticTestRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class DiagnosticExcelService {

    private static final String[] HEADERS = {
            "Service ID", "Slug Name", "Service Name", "Tags", "Parent Service ID", "Parent Service Name",
            "Category ID", "Category Name", "Department ID 1", "Department ID 2", "Department Name 1", "Department Name 2",
            "Gross Price", "Discount Price", "Net Price", "Original Amount", "Final Amount", "Taxation Amount", "Other Discount Amount",
            "Discount Type", "Brand ID", "Poc ID", "Schedule ID", "Schedule Type", "City ID", "Doctor ID", "Pin Code",
            "Home Collections", "Expiry Date", "Precaution", "Meta Title", "Meta Description", "Is Popular", "Is Active",
            "Diagnostic Category"
    };

    private final DiagnosticTestRepository testRepository;
    private final DiagnosticCategoryRepository categoryRepository;
    private final DiagnosticExcelRowProcessor rowProcessor;

    @Autowired
    public DiagnosticExcelService(DiagnosticTestRepository testRepository,
                                  DiagnosticCategoryRepository categoryRepository,
                                  DiagnosticExcelRowProcessor rowProcessor) {
        this.testRepository = testRepository;
        this.categoryRepository = categoryRepository;
        this.rowProcessor = rowProcessor;
    }

    /**
     * Exports all diagnostic tests/packages in the database to a beautifully structured Excel file
     * with dropdown validation constraints loaded dynamically from categories.
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportTestsToExcel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 1. Create Sheets
            XSSFSheet mainSheet = workbook.createSheet("Diagnostic Tests");
            XSSFSheet lookupSheet = workbook.createSheet("LookupLists");

            // 2. Fetch Categories for Drop-down
            List<DiagnosticCategory> categories = categoryRepository.findAll();
            List<String> categoryStrings = new ArrayList<>();
            for (DiagnosticCategory cat : categories) {
                categoryStrings.add(cat.getCategoryId() + " - " + cat.getCategoryName());
            }

            // Populate reference lookup sheet
            int rowIdx = 0;
            Row headerLookup = lookupSheet.createRow(rowIdx++);
            headerLookup.createCell(0).setCellValue("Categories");

            for (int i = 0; i < categoryStrings.size(); i++) {
                Row row = lookupSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(categoryStrings.get(i));
            }

            // Define Named Range
            Name nameCat = workbook.createName();
            nameCat.setNameName("CategoriesRange");
            nameCat.setRefersToFormula("LookupLists!$A$2:$A$" + Math.max(2, categoryStrings.size() + 1));

            // Hide Lookup sheet
            workbook.setSheetHidden(workbook.getSheetIndex(lookupSheet), true);

            // 3. Header Styling
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);

            // 4. Create Header Row
            Row headerRow = mainSheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // 5. Write Diagnostic Tests Data
            List<DiagnosticTest> tests = testRepository.findAll();
            int dataRowIdx = 1;
            for (DiagnosticTest test : tests) {
                Row row = mainSheet.createRow(dataRowIdx++);

                writeCell(row, 0, test.getServiceId());
                writeCell(row, 1, test.getSlugName());
                writeCell(row, 2, test.getServiceName());
                writeCell(row, 3, test.getTags());
                writeCell(row, 4, test.getParentServiceId());
                writeCell(row, 5, test.getParentServiceName());
                writeCell(row, 6, test.getCategoryId());
                writeCell(row, 7, test.getCategoryName());
                writeCell(row, 8, test.getDepartmentId1());
                writeCell(row, 9, test.getDepartmentId2());
                writeCell(row, 10, test.getDepartmentName1());
                writeCell(row, 11, test.getDepartmentName2());

                writeCell(row, 12, test.getGrossPrice());
                writeCell(row, 13, test.getDiscountPrice());
                writeCell(row, 14, test.getNetPrice());
                writeCell(row, 15, test.getOriginalAmount());
                writeCell(row, 16, test.getFinalAmount());
                writeCell(row, 17, test.getTaxationAmount());
                writeCell(row, 18, test.getOtherDiscountAmount());
                writeCell(row, 19, test.getDiscountType());

                writeCell(row, 20, test.getBrandId());
                writeCell(row, 21, test.getPocId());
                writeCell(row, 22, test.getScheduleId());
                writeCell(row, 23, test.getScheduleType());
                writeCell(row, 24, test.getCityId());
                writeCell(row, 25, test.getDoctorId());
                writeCell(row, 26, test.getPinCode());
                writeCell(row, 27, test.getHomeCollections());
                writeCell(row, 28, test.getExpiryDate());
                writeCell(row, 29, test.getPrecaution());
                writeCell(row, 30, test.getMetaTitle());
                writeCell(row, 31, test.getMetaDescription());
                writeCell(row, 32, test.getIsPopular());
                writeCell(row, 33, test.getIsActive());

                if (test.getDiagnosticCategory() != null) {
                    String catVal = test.getDiagnosticCategory().getCategoryId() + " - " + test.getDiagnosticCategory().getCategoryName();
                    writeCell(row, 34, catVal);
                } else {
                    writeCell(row, 34, "");
                }
            }

            // 6. Protection (Lock column A - Service ID)
            CellStyle unlockedStyle = workbook.createCellStyle();
            unlockedStyle.setLocked(false);

            CellStyle lockedIdStyle = workbook.createCellStyle();
            lockedIdStyle.setFont(headerFont);
            lockedIdStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            lockedIdStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            lockedIdStyle.setLocked(true);

            for (int r = 1; r <= 3000; r++) {
                Row dataRow = mainSheet.getRow(r);
                if (dataRow == null) dataRow = mainSheet.createRow(r);
                for (int c = 1; c < HEADERS.length; c++) {
                    Cell cell = dataRow.getCell(c);
                    if (cell == null) cell = dataRow.createCell(c);
                    cell.setCellStyle(unlockedStyle);
                }
            }

            for (int r = 1; r < dataRowIdx; r++) {
                Row dataRow = mainSheet.getRow(r);
                if (dataRow != null) {
                    Cell idCell = dataRow.getCell(0);
                    if (idCell == null) idCell = dataRow.createCell(0);
                    CellStyle idCellStyle = workbook.createCellStyle();
                    idCellStyle.cloneStyleFrom(lockedIdStyle);
                    idCellStyle.setFont(workbook.createFont());
                    idCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                    idCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    idCellStyle.setLocked(true);
                    idCell.setCellStyle(idCellStyle);
                }
            }

            mainSheet.protectSheet("GVK-ADMIN-2024");

            // 7. Validation constraints
            DataValidationHelper validationHelper = mainSheet.getDataValidationHelper();
            // Boolean choices
            addExplicitListValidation(mainSheet, validationHelper, new String[]{"TRUE", "FALSE"}, 32, 33);
            // Category Dropdown
            addFormulaListValidation(mainSheet, validationHelper, "CategoriesRange", 34, 34);

            // Column width resizing
            for (int col = 0; col < HEADERS.length; col++) {
                mainSheet.autoSizeColumn(col);
                int currentWidth = mainSheet.getColumnWidth(col);
                mainSheet.setColumnWidth(col, Math.max(currentWidth, 3500));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Diagnostic Excel file: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the uploaded Excel workbook, inserts/updates tests, and resolves relationships.
     */
    public Map<String, Object> importTestsFromExcel(InputStream is) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> details = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;
        int failedCount = 0;

        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip Header
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                rowNum++;
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue;
                }

                totalRows++;

                try {
                    String successMessage = rowProcessor.processRow(row, formatter, rowNum);
                    successCount++;
                    details.add(successMessage);
                } catch (Exception e) {
                    failedCount++;
                    details.add("Row " + rowNum + ": Failed - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel workbook: " + e.getMessage(), e);
        }

        result.put("totalRows", totalRows);
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("details", details);
        return result;
    }

    private void writeCell(Row row, int col, Object value) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value ? "TRUE" : "FALSE");
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private void addExplicitListValidation(Sheet sheet, DataValidationHelper helper, String[] list, int startCol, int endCol) {
        DataValidationConstraint constraint = helper.createExplicitListConstraint(list);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 3000, startCol, endCol);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private void addFormulaListValidation(Sheet sheet, DataValidationHelper helper, String formula, int startCol, int endCol) {
        DataValidationConstraint constraint = helper.createFormulaListConstraint(formula);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 3000, startCol, endCol);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setShowErrorBox(false); // allow custom entry if needed
        sheet.addValidationData(validation);
    }
}
