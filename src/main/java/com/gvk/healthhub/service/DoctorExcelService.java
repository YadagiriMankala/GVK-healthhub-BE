package com.gvk.healthhub.service;

import com.gvk.healthhub.entity.*;
import com.gvk.healthhub.repository.*;
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
import java.util.stream.Collectors;

@Service
public class DoctorExcelService {

    private static final String[] HEADERS = {
            "Doctor ID", "Title", "First Name", "Last Name", "Slug", "Gender", "Email", "Mobile",
            "Qualification", "Experience Years", "Practicing From Year", "Registration No",
            "Designation", "Description", "Image URL", "Walk-in Fee", "Video Fee",
            "Allows Video", "Allows Phone", "Is Featured", "Is Active",
            "Specialization 1", "Specialization 2",
            "Language 1", "Language 2", "Language 3",
            "Hospital 1", "Hospital 2"
    };

    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final LanguageRepository languageRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorExcelRowProcessor doctorExcelRowProcessor;

    @Autowired
    public DoctorExcelService(DoctorRepository doctorRepository,
                              SpecializationRepository specializationRepository,
                              LanguageRepository languageRepository,
                              HospitalRepository hospitalRepository,
                              DoctorExcelRowProcessor doctorExcelRowProcessor) {
        this.doctorRepository = doctorRepository;
        this.specializationRepository = specializationRepository;
        this.languageRepository = languageRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorExcelRowProcessor = doctorExcelRowProcessor;
    }

    /**
     * Exports all doctors in the database to a beautifully structured Excel file
     * with dropdown validation constraints loaded dynamically from reference tables.
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportDoctorsToExcel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 1. Create Sheets
            XSSFSheet mainSheet = workbook.createSheet("Doctors");
            XSSFSheet lookupSheet = workbook.createSheet("LookupLists");

            // 2. Fetch Reference Lists
            List<Specialization> specializations = specializationRepository.findAll();
            List<Language> languages = languageRepository.findAll();
            List<Hospital> hospitals = hospitalRepository.findAll();

            // Populate reference lookup sheet
            int rowIdx = 0;
            Row headerLookup = lookupSheet.createRow(rowIdx++);
            headerLookup.createCell(0).setCellValue("Specializations");
            headerLookup.createCell(1).setCellValue("Languages");
            headerLookup.createCell(2).setCellValue("Hospitals");

            int maxRows = Math.max(specializations.size(), Math.max(languages.size(), hospitals.size()));
            for (int i = 0; i < maxRows; i++) {
                Row row = lookupSheet.createRow(rowIdx++);
                if (i < specializations.size()) {
                    row.createCell(0).setCellValue(specializations.get(i).getName());
                }
                if (i < languages.size()) {
                    row.createCell(1).setCellValue(languages.get(i).getName());
                }
                if (i < hospitals.size()) {
                    row.createCell(2).setCellValue(hospitals.get(i).getName());
                }
            }

            // 3. Define Named Ranges for Dynamic Excel Drop-downs
            Name nameSpec = workbook.createName();
            nameSpec.setNameName("SpecializationsRange");
            nameSpec.setRefersToFormula("LookupLists!$A$2:$A$" + Math.max(2, specializations.size() + 1));

            Name nameLang = workbook.createName();
            nameLang.setNameName("LanguagesRange");
            nameLang.setRefersToFormula("LookupLists!$B$2:$B$" + Math.max(2, languages.size() + 1));

            Name nameHosp = workbook.createName();
            nameHosp.setNameName("HospitalsRange");
            nameHosp.setRefersToFormula("LookupLists!$C$2:$C$" + Math.max(2, hospitals.size() + 1));

            // Hide the reference lookup sheet from average admin to keep it clean
            workbook.setSheetHidden(workbook.getSheetIndex(lookupSheet), true);

            // 4. Style main sheet Header
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

            // 5. Create Header Row in Doctors sheet
            Row headerRow = mainSheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // 6. Write Doctors Data
            List<Doctor> doctors = doctorRepository.findAll();
            int docRowIdx = 1;
            for (Doctor doc : doctors) {
                Row row = mainSheet.createRow(docRowIdx++);

                writeCell(row, 0, doc.getId());
                writeCell(row, 1, doc.getTitle());
                writeCell(row, 2, doc.getFirstName());
                writeCell(row, 3, doc.getLastName());
                writeCell(row, 4, doc.getSlug());
                writeCell(row, 5, doc.getGender());
                writeCell(row, 6, doc.getEmail());
                writeCell(row, 7, doc.getMobile());
                writeCell(row, 8, doc.getQualification());
                writeCell(row, 9, doc.getExperienceYears());
                writeCell(row, 10, doc.getPracticingFromYear());
                writeCell(row, 11, doc.getRegistrationNumber());
                writeCell(row, 12, doc.getDesignation());
                writeCell(row, 13, doc.getDescription());
                writeCell(row, 14, doc.getImageUrl());
                writeCell(row, 15, doc.getConsultationFeeWalkin());
                writeCell(row, 16, doc.getConsultationFeeVideo());
                writeCell(row, 17, doc.getAllowsVideoConsultation());
                writeCell(row, 18, doc.getAllowsPhoneConsultation());
                writeCell(row, 19, doc.getIsFeatured());
                writeCell(row, 20, doc.getIsActive());

                // Mappings - Specializations (up to 2)
                int specColIdx = 21;
                for (int s = 0; s < 2; s++) {
                    if (doc.getDoctorSpecializations() != null && s < doc.getDoctorSpecializations().size()) {
                        writeCell(row, specColIdx + s, doc.getDoctorSpecializations().get(s).getSpecialization().getName());
                    } else {
                        writeCell(row, specColIdx + s, "");
                    }
                }

                // Mappings - Languages (up to 3)
                int langColIdx = 23;
                for (int l = 0; l < 3; l++) {
                    if (doc.getDoctorLanguages() != null && l < doc.getDoctorLanguages().size()) {
                        writeCell(row, langColIdx + l, doc.getDoctorLanguages().get(l).getLanguage().getName());
                    } else {
                        writeCell(row, langColIdx + l, "");
                    }
                }

                // Mappings - Hospitals (up to 2)
                int hospColIdx = 26;
                for (int h = 0; h < 2; h++) {
                    if (doc.getDoctorHospitals() != null && h < doc.getDoctorHospitals().size()) {
                        writeCell(row, hospColIdx + h, doc.getDoctorHospitals().get(h).getHospital().getName());
                    } else {
                        writeCell(row, hospColIdx + h, "");
                    }
                }
            }

            // 7. Lock Column A (Doctor ID) using sheet protection
            // Mark all cells as unlocked by default, then re-lock only column A
            CellStyle unlockedStyle = workbook.createCellStyle();
            unlockedStyle.setLocked(false);

            CellStyle lockedIdStyle = workbook.createCellStyle();
            lockedIdStyle.setFont(headerFont); // reuse header font style for ID col
            lockedIdStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            lockedIdStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            lockedIdStyle.setLocked(true);

            // Apply unlocked style to all data columns (B onward)
            for (int r = 1; r <= 2000; r++) {
                Row dataRow = mainSheet.getRow(r);
                if (dataRow == null) dataRow = mainSheet.createRow(r);
                for (int c = 1; c < HEADERS.length; c++) {
                    Cell dc = dataRow.getCell(c);
                    if (dc == null) dc = dataRow.createCell(c);
                    dc.setCellStyle(unlockedStyle);
                }
            }
            // Column A cells (Doctor ID) keep their default locked style
            // Re-apply a clean locked style to all existing ID cells
            for (int r = 1; r < docRowIdx; r++) {
                Row dataRow = mainSheet.getRow(r);
                if (dataRow != null) {
                    Cell idCell = dataRow.getCell(0);
                    if (idCell == null) idCell = dataRow.createCell(0);
                    CellStyle idCellStyle = workbook.createCellStyle();
                    idCellStyle.cloneStyleFrom(lockedIdStyle);
                    idCellStyle.setFont(workbook.createFont()); // plain font
                    idCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                    idCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    idCellStyle.setLocked(true);
                    idCell.setCellStyle(idCellStyle);
                }
            }

            // Enable sheet protection — only locked cells will be protected
            mainSheet.protectSheet("GVK-ADMIN-2024");

            // 8. Setup Data Validations (Excel dropdown lists)
            DataValidationHelper validationHelper = mainSheet.getDataValidationHelper();

            // Static choices validations (Title: Dr., Prof., Mr., Ms. | Gender: Male, Female, Other | Booleans)
            addExplicitListValidation(mainSheet, validationHelper, new String[]{"Dr.", "Prof.", "Mr.", "Ms."}, 1, 1);
            addExplicitListValidation(mainSheet, validationHelper, new String[]{"Male", "Female", "Other"}, 5, 5);
            addExplicitListValidation(mainSheet, validationHelper, new String[]{"TRUE", "FALSE"}, 17, 20);

            // Dynamic choices validations referencing Named Ranges (showErrorBox=false allows custom typed values)
            addFormulaListValidation(mainSheet, validationHelper, "SpecializationsRange", 21, 22);
            addFormulaListValidation(mainSheet, validationHelper, "LanguagesRange", 23, 25);
            addFormulaListValidation(mainSheet, validationHelper, "HospitalsRange", 26, 27);

            // Auto-adjust column sizes for beautiful layout
            for (int col = 0; col < HEADERS.length; col++) {
                mainSheet.autoSizeColumn(col);
                // Set safe default padding
                int currentWidth = mainSheet.getColumnWidth(col);
                mainSheet.setColumnWidth(col, Math.max(currentWidth, 3500));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the uploaded Excel workbook, inserts new doctors, updates existing doctors,
     * resolves and saves their related mappings, and registers new languages/specializations dynamically.
     * Running without @Transactional at the outer level allows saving successful rows even if other rows fail.
     */
    public Map<String, Object> importDoctorsFromExcel(InputStream is) {
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
                    // Delegate execution of single row processing to isolated transactions
                    String successMessage = doctorExcelRowProcessor.processRow(row, formatter, rowNum);
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

    // Helper to safely write cell contents dynamically
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

    // Helper check if row is empty
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

    private void addIfNotEmpty(List<String> list, String value) {
        if (value != null && !value.trim().isEmpty()) {
            list.add(value.trim());
        }
    }

    private void addExplicitListValidation(Sheet sheet, DataValidationHelper helper, String[] list, int startCol, int endCol) {
        DataValidationConstraint constraint = helper.createExplicitListConstraint(list);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 2000, startCol, endCol);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private void addFormulaListValidation(Sheet sheet, DataValidationHelper helper, String formula, int startCol, int endCol) {
        DataValidationConstraint constraint = helper.createFormulaListConstraint(formula);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 2000, startCol, endCol);
        DataValidation validation = helper.createValidation(constraint, addressList);
        // Allow user to type custom values (new specializations / hospitals / languages)
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);
    }

    // Safely reads Cell as String
    private String getStringValue(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        String val = formatter.formatCellValue(cell).trim();
        return val.isEmpty() ? null : val;
    }

    // Safely reads Cell as Integer
    private Integer getIntegerValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return (int) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Safely reads Cell as Long
    private Long getLongValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return (long) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Safely reads Cell as BigDecimal
    private BigDecimal getBigDecimalValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Safely reads Cell as Boolean
    private Boolean getBooleanValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        return "TRUE".equalsIgnoreCase(val) || "YES".equalsIgnoreCase(val) || "Y".equalsIgnoreCase(val);
    }
}
