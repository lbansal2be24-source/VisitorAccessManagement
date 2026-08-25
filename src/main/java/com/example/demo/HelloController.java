package com.example.demo;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.model.User;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    private final UserService userService;
    private final EmployeeService employeeService;

    public HelloController(
            UserService userService,
            EmployeeService employeeService) {

        this.userService = userService;
        this.employeeService = employeeService;
    }


    // =========================================================
    // VISITOR REGISTRATION
    // =========================================================

    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestBody @Valid User user) {

        User savedUser = userService.saveUser(user);

        Map<String, Object> response = new HashMap<>();

        response.put("id", savedUser.getId());

        response.put(
                "accessToken",
                savedUser.getAccessToken()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // VISITOR STATUS CHECK
    //
    // IMPORTANT:
    // Visitor only needs Request ID.
    // Access token is NOT required here.
    // =========================================================

    @GetMapping("/users/{id}/status")
    public ResponseEntity<?> getVisitorStatus(
            @PathVariable Integer id) {

        User user = userService.getUserById(id);

        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Request with ID " + id + " not found");
        }

        return ResponseEntity.ok(user);
    }


    // =========================================================
    // EMPLOYEE LIST
    // PUBLIC
    // =========================================================

    @GetMapping({
            "/employees",
            "/employee/list"
    })
    public ResponseEntity<?> getEmployees() {

        List<Employee> employees =
                employeeService.getAllEmployees();

        List<Map<String, String>> response =
                new ArrayList<>();

        for (Employee employee : employees) {

            Map<String, String> employeeData =
                    new HashMap<>();

            employeeData.put(
                    "employeeId",
                    employee.getEmployeeId()
            );

            employeeData.put(
                    "name",
                    employee.getName()
            );

            response.add(employeeData);
        }

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // EMPLOYEE LOGIN
    // =========================================================

    @PostMapping("/employee/login")
    public ResponseEntity<?> employeeLogin(
            @RequestBody Map<String, String> loginData,
            HttpSession session) {

        String employeeId =
                loginData.get("employeeId");

        String password =
                loginData.get("password");

        Employee employee =
                employeeService.login(
                        employeeId,
                        password
                );

        if (employee == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Invalid Employee ID or Password"
                    );
        }

        session.setAttribute(
                "employeeId",
                employee.getEmployeeId()
        );

        session.setAttribute(
                "employeeName",
                employee.getName()
        );

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "employeeId",
                employee.getEmployeeId()
        );

        response.put(
                "name",
                employee.getName()
        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // EMPLOYEE LOGOUT
    // =========================================================

    @PostMapping("/employee/logout")
    public ResponseEntity<?> employeeLogout(
            HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }


    // =========================================================
    // CURRENT LOGGED-IN EMPLOYEE
    // =========================================================

    @GetMapping("/employee/me")
    public ResponseEntity<?> getLoggedInEmployee(
            HttpSession session) {

        String employeeId =
                (String) session.getAttribute(
                        "employeeId"
                );

        String employeeName =
                (String) session.getAttribute(
                        "employeeName"
                );

        if (employeeId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Not logged in");
        }

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "employeeId",
                employeeId
        );

        response.put(
                "name",
                employeeName
        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET ALL VISITORS / REQUESTS
    //
    // EMPLOYEE ONLY
    // =========================================================

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            HttpSession session) {

        if (!isEmployeeLoggedIn(session)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }


    // =========================================================
    // GET SINGLE VISITOR
    // EMPLOYEE ONLY
    // =========================================================

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(
            @PathVariable Integer id,
            HttpSession session) {

        if (!isEmployeeLoggedIn(session)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }

        User user =
                userService.getUserById(id);

        if (user == null) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }

        return ResponseEntity.ok(user);
    }


    // =========================================================
    // UPDATE VISITOR
    // EMPLOYEE ONLY
    // =========================================================

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody @Valid User user,
            HttpSession session) {

        if (!isEmployeeLoggedIn(session)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }

        User updatedUser =
                userService.updateUser(
                        id,
                        user
                );

        if (updatedUser == null) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }

        return ResponseEntity.ok(
                updatedUser
        );
    }


    // =========================================================
    // DELETE VISITOR
    // EMPLOYEE ONLY
    // =========================================================

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Integer id,
            HttpSession session) {

        if (!isEmployeeLoggedIn(session)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }

        boolean deleted =
                userService.deleteUser(id);

        if (!deleted) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }

        return ResponseEntity
                .noContent()
                .build();
    }


    // =========================================================
    // APPROVE VISITOR
    // EMPLOYEE ONLY
    // =========================================================

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(
            @PathVariable Integer id,
            HttpSession session) {

        if (!isEmployeeLoggedIn(session)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }

        User updatedUser =
                userService.updateStatus(
                        id,
                        "APPROVED"
                );

        if (updatedUser == null) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }

        return ResponseEntity.ok(
                updatedUser
        );
    }


    // =========================================================
    // REJECT VISITOR
    // EMPLOYEE ONLY
    // =========================================================

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(
            @PathVariable Integer id,
            HttpSession session) {

        if (!isEmployeeLoggedIn(session)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }

        User updatedUser =
                userService.updateStatus(
                        id,
                        "REJECTED"
                );

        if (updatedUser == null) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }

        return ResponseEntity.ok(
                updatedUser
        );
    }


    // =========================================================
    // CHECK EMPLOYEE LOGIN
    // =========================================================

    private boolean isEmployeeLoggedIn(
            HttpSession session) {

        return session.getAttribute(
                "employeeId"
        ) != null;
    }
}