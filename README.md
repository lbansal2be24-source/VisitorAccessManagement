# Visitor Access Management System

A secure web-based Visitor Access Management System built using Java Spring Boot and MySQL.

## 📌 About the Project

This project manages visitor registration and access requests in an organization.

A visitor can submit a visit request by providing their details, purpose of visit, and the employee they want to meet. The selected employee can then review the request and either approve or reject it.

The system also allows visitors to check the current status of their request using the Request ID.

## ✨ Features

- Visitor registration
- Employee selection
- Visit request submission
- Employee login
- Employee dashboard
- Approve or reject visitor requests
- Request status tracking
- Request history
- MySQL database integration
- REST-based backend communication
- Exception handling

## 🛠️ Technologies Used

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- HTML
- CSS
- JavaScript
- Maven
- Git & GitHub

## 🔄 System Flow

1. Visitor opens the application.
2. Visitor enters their details.
3. Visitor selects the employee they want to meet.
4. Visitor submits the visit request.
5. The request is stored in the database.
6. The selected employee logs into the employee workspace.
7. Employee reviews the request.
8. Employee approves or rejects the request.
9. Visitor can check the request status using the Request ID.

## ▶️ How to Run

### Prerequisites

Make sure the following are installed:

- Java
- MySQL
- Maven

### Steps

1. Clone the repository.

2. Configure the MySQL database in:

```text
src/main/resources/application.properties
