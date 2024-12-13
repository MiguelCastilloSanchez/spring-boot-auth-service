# Authentication Service API

This repository contains a Spring Boot application that manages user authentication for a specific application. It provides endpoints for user registration, login, and logout, ensuring secure authentication with token-based mechanisms. This repository also contains the CI/CD Pipeline for building and deploying the service to Dockerhub and deployment repository at https://github.com/MiguelCastilloSanchez/helm-chart-app-services

---

## Endpoints

### **1. Register a new user**
**Endpoint**: `POST /auth/register`  
**Description**: This endpoint allows new users to register by providing their details in JSON format. The application validates the data before creating a new user account.

**Request Body**:  
```json
{
  "name": "string",
  "email": "string",
  "password": "string",
}
```

---

### **2. Authenticate user login**
**Endpoint**: `POST /auth/login`  
**Description**: Authenticates a user by verifying their email and password. If successful, it returns a JSON Web Token (JWT) to be used for subsequent authorized requests.

**Request Body**:  
```json
{
  "email": "string",
  "password": "string"
}
```

---

### **3. Logout a user**
**Endpoint**: `POST /auth/logout`  
**Description**: Logs out the user by revoking their JWT token. The token is removed from the system, ensuring it can no longer be used.

**Headers**:  
- `Authorization: Bearer <token>`

---