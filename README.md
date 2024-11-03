# Project Name - ms-category

> **A microservice for managing categories in a hierarchical tree structure, with caching, soft delete functionality, and resilience features.**

## Table of Contents

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Example Requests](#example-requests)
- [Example Response](#example-response)
- [API Endpoints](#api-endpoints)
- [Error Handling](#error-handling)
- [Configuration](#configuration)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [Future Enhancements](#future-enhancements)
- [Contributing](#contributing)
---
## Overview

**ms-category** is a microservice designed to manage categories in a hierarchical tree structure, enabling efficient organization of nested categories. It provides operations for creating, retrieving, and soft-deleting categories, with a Redis caching layer to optimize response times and a soft delete implementation to keep deleted categories for reference or audit purposes. The service also includes resilience mechanisms, such as retries and circuit breakers, to handle transient failures.

---
## Features

- **Tree-Structured Category Management**: Supports CRUD operations for categories organized in a parent-child tree structure.
- **Caching**: Utilizes Redis caching to reduce database load and improve response times.
- **Soft Delete**: Marks categories as deleted without removing them from the database.
- **Resilience**: Implements retry mechanisms and circuit breakers for improved reliability.
- **Health Checks**: Provides automated health checks to monitor service status.
- **Database Migrations with Liquibase**: Manages database schema changes consistently across environments.

---
## Technologies Used

- **Spring Boot**
- **Liquibase** (for database versioning)
- **Redis** (for caching)
- **NGINX** (as a reverse proxy)
- **Resilience4j** (for circuit breaker, retry, and rate limiting)
- **Lua** (for NGINX scripting)
- **PostgreSQL** (database)
- **Docker** (for containerization)
- **Spock Framework** (for testing)

---
## Architecture

This project uses a microservices architecture. The **ms-category** service provides an API for category management, supporting tree-structured data. NGINX serves as a reverse proxy with Lua scripting for enhanced control over request handling and error management.

The hierarchical structure of categories enables each category to have a **parent category**, forming a tree structure. **Base categories** have `null` as their `baseId`, while **subcategories** have a reference to their parent category via `baseId`. This structure allows for organized and nested categories with efficient retrieval and management capabilities.

![Architecture Diagram](./assets/architecture-diagram.png)

---
## Getting Started

---
### Prerequisites

- **Docker**: Ensure Docker is installed to run the services in containers.
- **Java 17** or higher.
- **Gradle**: For building the project.

---
### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-repo/ms-category.git
   cd ms-category
   ```

2. **Build the service**:
   ```bash
   ./gradlew clean build
   ```

3. **Set up environment variables** (or configure the `.env` file) for service URLs, database settings, and Redis configurations.

4. **Run the project using Docker Compose**:
   ```bash
   docker-compose up
   ```

5. **Access the application**: The service will be available at `http://localhost:<port>`, as configured in `docker-compose.yml`.

---
### Docker Compose

Here is a sample `docker-compose.yml` file that defines the ms-category service with dependencies on PostgreSQL and Redis:

```yaml
services:
   app:
      build: .
      image: ms-category
      ports:
         - "8081:8081"
      environment:
         SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/test
         SPRING_DATASOURCE_USERNAME: username
         SPRING_DATASOURCE_PASSWORD: password
         SPRING_REDIS_HOST: redis
      depends_on:
         - db
         - redis
      networks:
         - backend
      links:
         - db
         - redis
   db:
      image: postgres:latest
      ports:
         - "5432:5432"
      environment:
         POSTGRES_DB: test
         POSTGRES_USER: username
         POSTGRES_PASSWORD: password
      volumes:
         - db:/var/lib/postgresql/data
      networks:
         - backend

   redis:
      image: redis:latest
      ports:
         - "6379:6379"
      volumes:
         - redis_data:/data
      networks:
         - backend

   nginx:
      image: nginx:latest
      container_name: nginx
      volumes:
         - ./nginx.conf:/etc/nginx/nginx.conf
      ports:
         - "80:80"
      depends_on:
         - app
      networks:
         - backend

volumes:
   db:
      driver: local
   redis_data:
      driver: local

networks:
   backend:
      driver: bridge
```

---
## Usage

The project provides RESTful APIs for managing categories. Below are example requests for common operations.

---
### Example Requests

1. **Create Category** (supports both base categories and subcategories):
   ```http
   POST /v1/categories
   ```

   **Example Body**:
   ```json
   {
     "name": "Electronics",
     "baseId": null,  // null for base categories, or specify a base category ID for subcategories
     "picture": "electronics.jpg"
   }
   ```

2. **Get Categories**:
   ```http
   GET /v1/categories
   ```

---
### Example Response
   ```json
      [
        {
        "id": 1,
        "name": "Electronics",
        "baseId": null,
        "picture": "electronics.jpg",
        "status": "ACTIVE",
        "subCategories": [
        {
        "id": 2,
        "name": "Mobile Phones",
        "baseId": 1,
        "picture": "mobiles.jpg",
        "status": "ACTIVE",
        "subCategories": [
        {
        "id": 5,
        "name": "Smartphones",
        "baseId": 2,
        "picture": "smartphones.jpg",
        "status": "ACTIVE",
        "subCategories": []
        },
        {
        "id": 6,
        "name": "Feature Phones",
        "baseId": 2,
        "picture": "featurephones.jpg",
        "status": "ACTIVE",
        "subCategories": []
            }
          ]
        },
        {
        "id": 3,
        "name": "Laptops",
        "baseId": 1,
        "picture": "laptops.jpg",
        "status": "ACTIVE",
        "subCategories": []
          }
        ]
      },
      {
        "id": 4,
        "name": "Home Appliances",
        "baseId": null,
        "picture": "homeappliances.jpg",
        "status": "ACTIVE",
        "subCategories": [
            {
        "id": 7,
        "name": "Refrigerators",
        "baseId": 4,
        "picture": "refrigerators.jpg",
        "status": "ACTIVE",
        "subCategories": []
            }
          ]
        }
      ]
   ```

3. **Update Category**:
   ```http
   PUT /v1/categories/{categoryId}
   ```
   **Example Body**:
   ```json
   {
    "name": "Updated Category Name",
    "baseId": 1,            // Set to null if updating to a base category, or specify a new parent category ID
    "picture": "updated_picture.jpg",
    "status": "ACTIVE"       // Optional, if you are managing category status
   }
   ```

4. **Delete Category (Soft Delete)**:
   ```http
   DELETE /v1/categories/{categoryId}
   ```

Refer to [API Endpoints](#api-endpoints) for more details.

---
## API Endpoints

| Endpoint                     | Method | Description                                 |
|------------------------------|--------|---------------------------------------------|
| `/v1/categories`             | GET    | Retrieve all categories in a tree structure |
| `/v1/categories`             | POST   | Create a new category                       |
| `/v1/categories/{id}`        | PUT    | Update a category by ID                     |
| `/v1/categories/{id}`        | DELETE | Soft delete a category by ID                |
| `/actuator/health`           | GET    | Health check endpoint for monitoring        |

---
## Error Handling

This service includes error handling for common issues:
- **401 Forbidden**: Returned when authorization fails.
- **406 Not Acceptable**: Returned when the request does not meet required parameters.
- **500 Internal Server Error**: For unexpected server errors.

Refer to the error handling section in the code for details on each exception type and response.

---
## Configuration

Key configurations can be found in `application.yml` and `docker-compose.yml`:

- **Resilience4j** settings: Configured in `application.yml` to handle retry, circuit breaker, and rate limiting.
- **Caching**: Redis configuration in `application.yml` manages caching and cache expiration settings.
- **NGINX Configuration**: Controls proxy behavior, request handling, and custom error responses.

---
## Database Migrations

Database schema migrations are managed by **Liquibase** to ensure consistency across environments.

1. **ChangeLog Files**: Migration scripts are located in the `src/main/resources/liquibase` directory.
2. **Automatic Execution**: Liquibase migrations are automatically applied on application startup based on the configurations in `application.yml`.

---
### Example of a Liquibase ChangeLog
A sample Liquibase ChangeLog for the `categories` table might look like this:

```yaml
databaseChangeLog:
   - changeSet:
        id: creating-categories-table
        author: anar1501
        changes:
           - createTable:
                columns:
                   - column:
                        autoIncrement: true
                        constraints:
                           nullable: false
                           primaryKey: true
                           primaryKeyName: pk_categories
                        name: id
                        type: BIGINT
                   - column:
                        name: name
                        type: text
                        constraints:
                           nullable: false
                           unique: true
                   - column:
                        name: base_id
                        type: BIGINT
                        constraints:
                           foreignKeyName: fk_categories_base
                           references: categories(id)
                   - column:
                        name: picture
                        type: text
                        constraints:
                           nullable: false
                   - column:
                        name: status
                        type: VARCHAR(16)
                   - column:
                        name: created_at
                        type: DATETIME
                   - column:
                        name: updated_at
                        type: DATETIME
                tableName: categories
```

---
## Testing

- **Unit Tests**: The service includes unit tests using the Spock Framework.
- **Health Checks**: Monitored using the `/actuator/health` endpoint.

To run the test suite, use the following command:

```bash
./gradlew test
```

---
## Future Enhancements

- **Enhanced Error Logging**: Implement detailed error logs for better debugging.
- **Role-Based Access Control**: Allow specific actions based on user roles.
- **Rate Limiting on Endpoints**: Add rate limiting to prevent misuse of the API.

---
## Contributing

1. **Fork** the repository.
2. **Create a feature branch**: `git checkout -b feature-name`.
3. **Commit your changes**: `git commit -m 'Add some feature'`.
4. **Push to the branch**: `git push origin feature-name`.
5. **Create a Pull Request**.

**Note:** For major changes, please open an issue first to discuss what you would like to change.