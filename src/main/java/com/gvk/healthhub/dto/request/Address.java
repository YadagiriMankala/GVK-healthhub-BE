package com.gvk.healthhub.dto.request;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "locality")
    private String locality;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "pin_code")
    private String pincode;

    @Column(name = "name")
    private String name;

}
