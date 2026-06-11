package com.gvk.healthhub.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPocDTO {
    private Long pocId;
    private String pocName;
    private AddressDTO address;
    private String locality;
    private String areaName;
    private String email;
    private List<String> contactList;
    private Long brandId;
    private String brandName;
    private Integer pdfHeaderType;
    private Boolean payOnDeliveryAvailable;
    private Boolean pharmacyHomeDeliveryAvailable;
    private Boolean diagnosticSampleCollectionAvailable;
    private Boolean productWalkinAvailable;
    private Boolean productHomeDeliveryAvailable;
    private Boolean localDiagnosticPartner;
    private Boolean localPharmacyPartner;
    private Boolean receptionistAvailable;
    private Boolean pharmacyWalkinAvailable;
    private Boolean diagnosticWalkinAvailable;
    private Boolean centralPoc;
    private CdssOptionsDTO cdssOptions;
    private Boolean hasDigi;
    private Boolean disablePOC;
    private List<AvailableDaysListDTO> availableDaysList;
    private Double consultationFee;
    private Double videoLaterConsultationFee;
    private AgreementDTO agreement;
    private List<String> pocImageUrls;
    private Integer pocType;
    private String discountText;
    private Boolean scanAndUploadPrescriptions;
    private String pocSlug;
    private List<Object> serviceList;
    @Builder.Default
    private Boolean isSelected = false;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private Long addressId;
        private String doorNo;
        private String address1;
        private String address2;
        private String cityName;
        private String stateName;
        private String areaName;
        private String pinCode;
        private String locality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CdssOptionsDTO {
        private Boolean doctorEditable;
        private Boolean doctorSpecific;
        private Boolean brandSpecific;
        private Boolean brandDefaults;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableDaysListDTO {
        private Integer day;
        private List<TimeDTO> time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeDTO {
        private Integer from;
        private Integer to;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgreementDTO {
        private List<Long> packageIdList;
    }
}
