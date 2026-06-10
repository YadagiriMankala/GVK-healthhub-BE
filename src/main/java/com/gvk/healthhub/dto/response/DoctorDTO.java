package com.gvk.healthhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorDTO {

    private Long empId;
    private String slugName;
    private String title;
    private String firstName;
    private String lastName;
    private String gender;
    private List<String> contactList;
    private String emailId;
    private String qualificationName;
    private String experience;
    private Integer practicingFromYear;
    private String registrationNumber;
    private String designation;
    private List<LanguageDTO> languages;
    private Long serviceId;
    private String serviceName;
    private List<ServiceDTO> serviceList;
    private PocDetailsDTO pocDetails;
    private BigDecimal avgRating;
    private Integer totalUserCount;
    private String imageUrl;
    private Boolean isVideoSelected;
    private Double distance;
    private Boolean havingFutureSlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LanguageDTO {
        private Long languageId;
        private String languageName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceDTO {
        private Long serviceId;
        private String serviceName;
        private BigDecimal consultationFee;
        private BigDecimal videoConsultationFee;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PocDetailsDTO {
        private Long pocId;
        private String pocName;
        private AddressDTO address;
        private List<String> contactList;
        private BigDecimal consultationFee;
        private BigDecimal videoLaterConsultationFee;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddressDTO {
        private String addressLine1;
        private String locality;
        private String city;
        private String state;
        private String pinCode;
    }
}