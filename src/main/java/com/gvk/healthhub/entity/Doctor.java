package com.gvk.healthhub.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;



@Entity
@Table(
name = "t_doctor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "doctor_id")
private Long id;

@Column(name = "slug", length = 255, unique = true)
private String slug;

@Column(name = "title", length = 20)
private String title;

@Column(name = "first_name", length = 100)
private String firstName;

@Column(name = "last_name", length = 100)
private String lastName;

@Column(name = "gender", length = 20)
private String gender;

@Column(name = "email_id", length = 255)
private String email;

@Column(name = "mobile_number", length = 20)
private String mobile;

@Column(name = "qualification", length = 500)
private String qualification;

@Column(name = "experience_years")
private Integer experienceYears;

@Column(name = "practicing_from_year")
private Integer practicingFromYear;

@Column(name = "registration_number", length = 100)
private String registrationNumber;

@Column(name = "designation", length = 255)
private String designation;

@Column(name = "description", columnDefinition = "TEXT")
private String description;

@Column(name = "image_url", length = 1000)
private String imageUrl;

@Column(name = "avg_rating", precision = 3, scale = 2)
private BigDecimal avgRating;

@Column(name = "total_reviews")
private Integer totalReviews;

@Column(name = "consultation_fee_walkin", precision = 10, scale = 2)
private BigDecimal consultationFeeWalkin;

@Column(name = "consultation_fee_video", precision = 10, scale = 2)
private BigDecimal consultationFeeVideo;

@Column(name = "allows_video_consultation")
private Boolean allowsVideoConsultation;

@Column(name = "allows_phone_consultation")
private Boolean allowsPhoneConsultation;

@Column(name = "is_featured")
private Boolean isFeatured;

@Column(name = "is_active")
private Boolean isActive;

@CreationTimestamp
@Column(name = "created_date", updatable = false)
private LocalDateTime createdDate;

@UpdateTimestamp
@Column(name = "updated_date")
private LocalDateTime updatedDate;

@OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
@Builder.Default
private List<DoctorLanguage> doctorLanguages = new ArrayList<>();

@OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
@Builder.Default
private List<DoctorSpecialization> doctorSpecializations = new ArrayList<>();

@OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
@Builder.Default
private List<DoctorHospital> doctorHospitals = new ArrayList<>();

}

