package com.example.demo.repository;

import com.example.demo.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository
        extends JpaRepository<User, Integer> {


    /*
     * Check whether an email already exists.
     */
    boolean existsByEmail(String email);


    /*
     * Visitor security:
     *
     * A visitor can access a request only when
     * BOTH request ID and private access token match.
     */
    Optional<User> findByIdAndAccessToken(
            Integer id,
            String accessToken
    );


    /*
     * Get all requests assigned to a particular employee.
     *
     * Example:
     *
     * EMP001 -> only EMP001's requests
     * EMP002 -> only EMP002's requests
     */
    List<User> findByAssignedEmployeeId(
            String assignedEmployeeId
    );
}