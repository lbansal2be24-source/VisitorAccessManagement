package com.example.demo;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

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

        Employee employee = employeeRepository
                .findByEmployeeId(employeeId)
                .orElse(null);

        if (employee == null) {

            employee = new Employee(
                    employeeId,
                    name,
                    password
            );

        } else {

            // Keep existing employee ID
            // but make sure prototype credentials are correct.
            employee.setName(name);
            employee.setPassword(password);
        }

        employeeRepository.save(employee);
    }
}