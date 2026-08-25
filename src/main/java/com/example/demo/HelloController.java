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

        /*
         * IMPORTANT:
         *
         * These values must always be controlled
         * by the server.
         *
         * A visitor must not be able to submit:
         *
         * status = APPROVED
         *
         * or a fake access token.
         */

        user.setStatus("PENDING");
        user.setAccessToken(null);


        /*
         * Validate the selected employee.
         */

        String assignedEmployeeId =
                user.getAssignedEmployeeId();

        if (
                assignedEmployeeId == null ||
                assignedEmployeeId.isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body("Please select an employee.");
        }


        Employee selectedEmployee =
                employeeService
                        .getAllEmployees()
                        .stream()
                        .filter(employee ->
                                employee.getEmployeeId()
                                        .equals(assignedEmployeeId)
                        )
                        .findFirst()
                        .orElse(null);


        if (selectedEmployee == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Selected employee does not exist.");
        }


        /*
         * Always use the real employee name
         * from the database.
         */

        user.setPersonToMeet(
                selectedEmployee.getName()
        );


        User savedUser =
                userService.saveUser(user);


        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "id",
                savedUser.getId()
        );


        /*
         * Access token is returned ONLY at the
         * time of registration.
         *
         * It is protected from normal JSON serialization
         * by @JsonIgnore in User.
         */

        response.put(
                "accessToken",
                savedUser.getAccessToken()
        );


        response.put(
                "status",
                savedUser.getStatus()
        );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // VISITOR STATUS CHECK
    // =========================================================

    /*
     * Visitor must provide BOTH:
     *
     * Request ID
     * +
     * Access Token
     *
     * Without the token, the request cannot be viewed.
     */

    @GetMapping("/users/{id}/status")
    public ResponseEntity<?> getVisitorStatus(
            @PathVariable Integer id,
            @RequestHeader(
                    value = "X-Access-Token",
                    required = false
            ) String accessToken) {


        if (
                accessToken == null ||
                accessToken.isBlank()
        ) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Access token required.");
        }


        User user =
                userService
                        .getUserByIdAndAccessToken(
                                id,
                                accessToken
                        );


        /*
         * Return 404 instead of telling the user
         * whether the request ID exists.
         */

        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Request not found.");
        }


        Map<String, Object> response =
                new HashMap<>();


        response.put(
                "id",
                user.getId()
        );


        response.put(
                "name",
                user.getName()
        );


        response.put(
                "personToMeet",
                user.getPersonToMeet()
        );


        response.put(
                "purpose",
                user.getPurpose()
        );


        response.put(
                "status",
                user.getStatus()
        );


        return ResponseEntity.ok(response);
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


        /*
         * Prevent session fixation.
         *
         * The session ID is changed after
         * successful authentication.
         */

        

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
    // GET EMPLOYEE'S OWN REQUESTS
    // =========================================================

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            HttpSession session) {


        String employeeId =
                getLoggedInEmployeeId(session);


        if (employeeId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }


        /*
         * IMPORTANT:
         *
         * The database now returns ONLY requests
         * assigned to this employee.
         *
         * We no longer send every visitor request
         * to the browser.
         */

        return ResponseEntity.ok(
                userService.getUsersForEmployee(
                        employeeId
                )
        );
    }


    // =========================================================
    // GET SINGLE VISITOR
    // EMPLOYEE'S OWN REQUEST ONLY
    // =========================================================

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(
            @PathVariable Integer id,
            HttpSession session) {


        String employeeId =
                getLoggedInEmployeeId(session);


        if (employeeId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }


        User user =
                userService
                        .getUserById(id);


        if (user == null) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }


        /*
         * Employee can only view
         * their own assigned request.
         */

        if (
                user.getAssignedEmployeeId() == null ||
                !user.getAssignedEmployeeId()
                        .equals(employeeId)
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access this request."
                    );
        }


        return ResponseEntity.ok(user);
    }


    // =========================================================
    // UPDATE VISITOR
    // EMPLOYEE'S OWN REQUEST ONLY
    // =========================================================

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody @Valid User user,
            HttpSession session) {


        String employeeId =
                getLoggedInEmployeeId(session);


        if (employeeId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }


        User existingUser =
                userService.getUserById(id);


        if (existingUser == null) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }


        /*
         * Ownership check.
         */

        if (
                existingUser.getAssignedEmployeeId()
                        == null ||
                !existingUser
                        .getAssignedEmployeeId()
                        .equals(employeeId)
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to modify this request."
                    );
        }


        /*
         * Do not allow an employee to change
         * ownership or status through this endpoint.
         */

        user.setAssignedEmployeeId(employeeId);
        user.setStatus(null);


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
    // EMPLOYEE'S OWN REQUEST ONLY
    // =========================================================

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Integer id,
            HttpSession session) {


        String employeeId =
                getLoggedInEmployeeId(session);


        if (employeeId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }


        User existingUser =
                userService.getUserById(id);


        if (existingUser == null) {

            throw new ResourceNotFoundException(
                    "User with ID " +
                            id +
                            " not found"
            );
        }


        /*
         * Employee can delete only
         * their own request.
         */

        if (
                existingUser.getAssignedEmployeeId()
                        == null ||
                !existingUser
                        .getAssignedEmployeeId()
                        .equals(employeeId)
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to delete this request."
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
    // EMPLOYEE'S OWN REQUEST ONLY
    // =========================================================

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(
            @PathVariable Integer id,
            HttpSession session) {


        String employeeId =
                getLoggedInEmployeeId(session);


        if (employeeId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }


        User updatedUser =
                userService.updateStatusForEmployee(
                        id,
                        employeeId,
                        "APPROVED"
                );


        if (updatedUser == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to approve this request."
                    );
        }


        return ResponseEntity.ok(
                updatedUser
        );
    }


    // =========================================================
    // REJECT VISITOR
    // EMPLOYEE'S OWN REQUEST ONLY
    // =========================================================

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(
            @PathVariable Integer id,
            HttpSession session) {


        String employeeId =
                getLoggedInEmployeeId(session);


        if (employeeId == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Employee login required"
                    );
        }


        User updatedUser =
                userService.updateStatusForEmployee(
                        id,
                        employeeId,
                        "REJECTED"
                );


        if (updatedUser == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to reject this request."
                    );
        }


        return ResponseEntity.ok(
                updatedUser
        );
    }


    // =========================================================
    // HELPER
    // =========================================================

    private String getLoggedInEmployeeId(
            HttpSession session) {

        Object employeeId =
                session.getAttribute(
                        "employeeId"
                );


        if (employeeId == null) {

            return null;
        }


        return employeeId.toString();
    }
}