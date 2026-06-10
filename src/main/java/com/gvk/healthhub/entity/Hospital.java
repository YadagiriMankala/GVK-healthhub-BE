package com.gvk.healthhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_hospital")
@Data
public class Hospital {

    @Id
    private Long id;

    private String name;

    private String addressLine1;

    private String locality;

    private String city;

    private String state;

    private String pincode;

    private String phone;
}