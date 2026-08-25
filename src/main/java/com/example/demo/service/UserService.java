package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserService {


    private final UserRepository userRepository;


    @PersistenceContext
    private EntityManager entityManager;


    public UserService(
            UserRepository userRepository) {

        this.userRepository =
                userRepository;
    }


    // ============================================================
    // GET ALL USERS
    // ============================================================

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }


    // ============================================================
    // GET USER BY ID
    // ============================================================

    public User getUserById(Integer id) {

        return userRepository
                .findById(id)
                .orElse(null);
    }


    // ============================================================
    // VISITOR SECURITY
    // ============================================================

    /*
     * Visitor can access a request only when:
     *
     * Request ID
     * +
     * Access Token
     *
     * both match.
     */
    public User getUserByIdAndAccessToken(
            Integer id,
            String accessToken) {

        return userRepository
                .findByIdAndAccessToken(
                        id,
                        accessToken
                )
                .orElse(null);
    }


    // ============================================================
    // GET REQUESTS FOR ONE EMPLOYEE
    // ============================================================

    /*
     * IMPORTANT:
     *
     * This method is used by the employee dashboard.
     *
     * It returns ONLY requests assigned to
     * the logged-in employee.
     */
    public List<User> getUsersForEmployee(
            String employeeId) {

        return userRepository
                .findByAssignedEmployeeId(
                        employeeId
                );
    }


    // ============================================================
    // SAVE USER
    // ============================================================

    public User saveUser(User user) {

        return userRepository.save(user);
    }


    // ============================================================
    // UPDATE USER
    // ============================================================

    public User updateUser(
            Integer id,
            User user) {

        User existingUser =
                userRepository
                        .findById(id)
                        .orElse(null);


        if (existingUser == null) {

            return null;
        }


        existingUser.setName(
                user.getName()
        );


        existingUser.setEmail(
                user.getEmail()
        );


        existingUser.setPhone(
                user.getPhone()
        );


        existingUser.setPurpose(
                user.getPurpose()
        );


        existingUser.setPersonToMeet(
                user.getPersonToMeet()
        );


        /*
         * Status can be updated only when
         * a status value is provided.
         */
        if (user.getStatus() != null) {

            existingUser.setStatus(
                    user.getStatus()
            );
        }


        /*
         * Employee assignment can also be
         * updated when explicitly provided.
         */
        if (
                user.getAssignedEmployeeId()
                        != null
        ) {

            existingUser.setAssignedEmployeeId(
                    user.getAssignedEmployeeId()
            );
        }


        return userRepository.save(
                existingUser
        );
    }


    // ============================================================
    // DELETE USER
    // ============================================================

    public boolean deleteUser(Integer id) {

        if (
                !userRepository.existsById(id)
        ) {

            return false;
        }


        userRepository.deleteById(id);

        return true;
    }


    // ============================================================
    // UPDATE STATUS — GENERAL
    // ============================================================

    @Transactional
    public User updateStatus(
            Integer id,
            String status) {


        User user =
                userRepository
                        .findById(id)
                        .orElse(null);


        if (user == null) {

            return null;
        }


        entityManager.createQuery(
                "UPDATE User u " +
                "SET u.status = :status " +
                "WHERE u.id = :id"
        )
        .setParameter(
                "status",
                status
        )
        .setParameter(
                "id",
                id
        )
        .executeUpdate();


        return userRepository
                .findById(id)
                .orElse(null);
    }


    // ============================================================
    // UPDATE STATUS — SECURE EMPLOYEE VERSION
    // ============================================================

    /*
     * THIS IS VERY IMPORTANT.
     *
     * An employee cannot approve/reject
     * another employee's request.
     *
     * First we find the request.
     *
     * Then we verify:
     *
     * request.assignedEmployeeId
     *              ==
     * loggedInEmployeeId
     *
     * Only then do we change the status.
     */
    @Transactional
    public User updateStatusForEmployee(
            Integer id,
            String employeeId,
            String status) {


        User user =
                userRepository
                        .findById(id)
                        .orElse(null);


        /*
         * Request does not exist.
         */
        if (user == null) {

            return null;
        }


        /*
         * SECURITY CHECK
         *
         * Employee can modify ONLY
         * their own assigned requests.
         */
        if (
                user.getAssignedEmployeeId()
                        == null
                ||
                !user.getAssignedEmployeeId()
                        .equals(employeeId)
        ) {

            return null;
        }


        /*
         * Only allow valid statuses.
         */
        if (
                !"PENDING".equals(status)
                &&
                !"APPROVED".equals(status)
                &&
                !"REJECTED".equals(status)
        ) {

            return null;
        }


        entityManager.createQuery(
                "UPDATE User u " +
                "SET u.status = :status " +
                "WHERE u.id = :id " +
                "AND u.assignedEmployeeId = :employeeId"
        )
        .setParameter(
                "status",
                status
        )
        .setParameter(
                "id",
                id
        )
        .setParameter(
                "employeeId",
                employeeId
        )
        .executeUpdate();


        return userRepository
                .findById(id)
                .orElse(null);
    }
}