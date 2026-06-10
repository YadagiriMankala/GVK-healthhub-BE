package com.gvk.healthhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalDTO {
    private Long pocId;
    private String pocName;
    private String pocSlug;
    private AddressDTO address;
    private List<String> contactList;
    private String email;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private String address1;
        private String address2;
        private String locality;
        private String cityName;
        private String stateName;
        private String pinCode;
    }
}
