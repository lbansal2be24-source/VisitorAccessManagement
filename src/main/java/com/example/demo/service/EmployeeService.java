package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public EmployeeService(
            EmployeeRepository employeeRepository) {

        this.employeeRepository =
                employeeRepository;
    }


    // =========================================================
    // EMPLOYEE LOGIN
    // =========================================================

    public Employee login(
            String employeeId,
            String password) {

        if (employeeId == null ||
                employeeId.isBlank() ||
                password == null ||
                password.isBlank()) {

            return null;
        }

        Employee employee =
                employeeRepository
                        .findByEmployeeId(employeeId)
                        .orElse(null);

        if (employee == null) {
            return null;
        }

        String storedPassword =
                employee.getPassword();

        /*
         * New passwords are stored using BCrypt.
         */
        if (storedPassword != null &&
                storedPassword.startsWith("$2")) {

            if (!passwordEncoder.matches(
                    password,
                    storedPassword)) {

                return null;
            }

            return employee;
        }

        /*
         * This block is only for existing prototype
         * accounts whose passwords were stored as plain text.
         *
         * After successful login, the password is
         * immediately converted to BCrypt.
         */
        if (storedPassword != null &&
                storedPassword.equals(password)) {

            employee.setPassword(
                    passwordEncoder.encode(password)
            );

            employeeRepository.save(employee);

            return employee;
        }

        return null;
    }


    // =========================================================
    // GET ALL EMPLOYEES
    // =========================================================

    public List<Employee> getAllEmployees() {

        return employeeRepository.findAll();
    }
}