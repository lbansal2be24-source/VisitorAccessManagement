package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;


@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @NotBlank(message = "Visitor name is required")
    private String name;


    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;


    @NotBlank(message = "Phone number is required")
    private String phone;


    @NotBlank(message = "Purpose of visit is required")
    private String purpose;


    @NotBlank(message = "Person to meet is required")
    private String personToMeet;


    /*
     * Current request status.
     *
     * PENDING
     * APPROVED
     * REJECTED
     */
    private String status;


    /*
     * Employee to whom this request belongs.
     *
     * Example:
     *
     * EMP001
     * EMP002
     * EMP003
     *
     * This is the IMPORTANT security field.
     */
    @Column(
        nullable = false,
        length = 100
    )
    private String assignedEmployeeId;


    /*
     * Private token belonging to the visitor.
     *
     * @JsonIgnore ensures that employee APIs
     * never expose this token.
     */
    @JsonIgnore
    @Column(
        nullable = false,
        unique = true,
        updatable = false,
        length = 36
    )
    private String accessToken;


    /*
     * Default constructor.
     */
    public User() {

        this.status = "PENDING";
    }


    /*
     * Generate visitor access token automatically.
     */
    @PrePersist
    public void generateAccessToken() {

        if (this.accessToken == null) {

            this.accessToken =
                    UUID.randomUUID().toString();
        }
    }


    // ============================================================
    // ID
    // ============================================================

    public Integer getId() {

        return id;
    }


    public void setId(Integer id) {

        this.id = id;
    }


    // ============================================================
    // NAME
    // ============================================================

    public String getName() {

        return name;
    }


    public void setName(String name) {

        this.name = name;
    }


    // ============================================================
    // EMAIL
    // ============================================================

    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    // ============================================================
    // PHONE
    // ============================================================

    public String getPhone() {

        return phone;
    }


    public void setPhone(String phone) {

        this.phone = phone;
    }


    // ============================================================
    // PURPOSE
    // ============================================================

    public String getPurpose() {

        return purpose;
    }


    public void setPurpose(String purpose) {

        this.purpose = purpose;
    }


    // ============================================================
    // PERSON TO MEET
    // ============================================================

    public String getPersonToMeet() {

        return personToMeet;
    }


    public void setPersonToMeet(
            String personToMeet) {

        this.personToMeet = personToMeet;
    }


    // ============================================================
    // STATUS
    // ============================================================

    public String getStatus() {

        return status;
    }


    public void setStatus(String status) {

        this.status = status;
    }


    // ============================================================
    // ASSIGNED EMPLOYEE
    // ============================================================

    public String getAssignedEmployeeId() {

        return assignedEmployeeId;
    }


    public void setAssignedEmployeeId(
            String assignedEmployeeId) {

        this.assignedEmployeeId =
                assignedEmployeeId;
    }


    // ============================================================
    // ACCESS TOKEN
    // ============================================================

    public String getAccessToken() {

        return accessToken;
    }


    public void setAccessToken(
            String accessToken) {

        this.accessToken = accessToken;
    }
}