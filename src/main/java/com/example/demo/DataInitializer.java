package com.example.demo;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();


    @Bean
    CommandLineRunner initializeEmployees(
            EmployeeRepository employeeRepository) {

        return args -> {

            createOrUpdateEmployee(
                    employeeRepository,
                    "EMP001",
                    "Harshil",
                    "1234"
            );

            createOrUpdateEmployee(
                    employeeRepository,
                    "EMP002",
                    "Rahul",
                    "1234"
            );

            createOrUpdateEmployee(
                    employeeRepository,
                    "EMP003",
                    "Aman",
                    "1234"
            );

            createOrUpdateEmployee(
                    employeeRepository,
                    "EMP004",
                    "Priya",
                    "1234"
            );
        };
    }


    private void createOrUpdateEmployee(
            EmployeeRepository employeeRepository,
            String employeeId,
            String name,
            String password) {

        Employee employee =
                employeeRepository
                        .findByEmployeeId(employeeId)
                        .orElse(null);


        if (employee == null) {

            /*
             * New employee:
             * Store BCrypt password.
             */
            employee = new Employee(
                    employeeId,
                    name,
                    passwordEncoder.encode(password)
            );

        } else {

            employee.setName(name);

            /*
             * Do NOT hash an already-hashed password again.
             */
            if (employee.getPassword() == null ||
                    !employee.getPassword().startsWith("$2")) {

                employee.setPassword(
                        passwordEncoder.encode(password)
                );
            }
        }

        employeeRepository.save(employee);
    }
}