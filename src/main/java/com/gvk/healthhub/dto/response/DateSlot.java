package com.gvk.healthhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DateSlot {
    private Long time;
    private Integer status;
    private Integer vacantSlots;
    private Long expireTime;
    private Integer bookingSource;
    private Integer bookingType;
    private Integer bookingSubType;
    private Integer typeOfAppointment;
    private String slotToken;
    private Integer slotPreferredMode;
    private Double slotPrice;
}
