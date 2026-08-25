package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository) {

        this.employeeRepository =
                employeeRepository;
    }

    // ========================================
    // EMPLOYEE LOGIN
    // ========================================

    public Employee login(
            String employeeId,
            String password) {

        Employee employee =
                employeeRepository
                        .findByEmployeeId(employeeId)
                        .orElse(null);

        if (employee == null) {
            return null;
        }

        if (!employee.getPassword().equals(password)) {
            return null;
        }

        return employee;
    }


    // ========================================
    // GET ALL EMPLOYEES
    // ========================================
    //
    // Used by the Visitor Portal.
    //
    // Returns the employee list so that a visitor
    // can select the person they want to meet.
    //
    // ========================================

    public List<Employee> getAllEmployees() {

        return employeeRepository.findAll();
    }
}