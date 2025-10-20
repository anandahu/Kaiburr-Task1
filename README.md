

# Task Manager API

A RESTful Spring Boot API for managing, executing, and tracking shell-based tasks with MongoDB-backed history and strong safety safeguards.

---

## Overview

The Task Manager API enables users to define, organize, and remotely execute shell commands ("tasks"), with every execution persisting relevant metadata: start and end time plus output. This tool simplifies automation, auditing, and delegation of shell operations.

---

## Features

- CRUD operations for tasks (Create, Read, Update, Delete).
- Execute assigned shell commands safely and remotely.
- Complete execution history within each task (timestamps, output).
- Search tasks by name (case-insensitive).
- Built-in safety checks to avoid risky commands like `rm`, `sudo`, `shutdown`.
- Cross-platform support: works on Windows and Unix-like systems.

---

## Technologies Used

- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data MongoDB
- Maven (with Maven Wrapper)
- MongoDB

---

## Project Structure

The main codebase is organized as follows:



src/main/java/com/kaiburr/taskmanager/
├── TaskmanagerApplication.java // Main Spring Boot entry
├── controller/
│ └── TaskController.java // REST API endpoints
├── model/
│ ├── Task.java // Task definition/document
│ └── TaskExecution.java // Embedded execution history
└── repository/



---

## Data Models

### Task.java

| Field            | Type                  | Annotation     | Description                                   |
|------------------|-----------------------|----------------|-----------------------------------------------|
| id               | String                | @Id            | Unique MongoDB identifier                     |
| name             | String                |                | User-defined task name                        |
| owner            | String                |                | Task owner/creator                            |
| command          | String                |                | Associated shell command                      |
| taskExecutions   | List<TaskExecution>    |                | Historical executions (embedded list)         |

### TaskExecution.java

| Field      | Type    | Description                                  |
|------------|---------|----------------------------------------------|
| startTime  | Date    | Timestamp for execution start                |
| endTime    | Date    | Timestamp for execution end                  |
| output     | String  | Collected stdout/stderr from shell process   |

---

## API Endpoints

All endpoints use `/tasks` as the prefix and support CORS for all domains.

| Method & Path            | Description                                                               |
|--------------------------|---------------------------------------------------------------------------|
| GET /tasks               | List all tasks                                                            |
| GET /tasks/{id}          | Retrieve single task by ID                                                |
| GET /tasks/search        | Search tasks by name (case-insensitive, query: `name`)                    |
| PUT /tasks               | Create/update a task; validates for safe commands                         |
| DELETE /tasks/{id}       | Delete a task by ID                                                       |
| PUT /tasks/{id}/execute  | Execute the shell command of a task; add execution to history             |

Example request/response structures are available in the original README screenshots.

---

## Code Deep Dive

### `TaskController.java` Highlights

- `executeTask(id)`:
    - Looks up task, sets execution `startTime`
    - Uses OS detection (`System.getProperty("os.name")`)
        - Windows: `cmd.exe /c [command]`
        - Unix/Linux/Mac: `sh -c [command]`
    - Captures both stdout and stderr merged
    - Timeout: forcibly kills after 60 seconds, annotates output
    - Saves updated execution to MongoDB
    - Returns modified Task object with history

- `isUnsafe(command)`:
    - Forbids commands containing substrings: `"rm "`, `"mv "`, `"sudo "`, `"shutdown"`, `"reboot"`, `"dd "` (case-insensitive)
    - Returns HTTP 400 if unsafe

### `TaskRepository.java`

- Extends Spring Data's MongoRepository for auto-CRUD
- Custom search: `findByNameContainingIgnoreCase(name)` for flexible lookups

---

## Configuration

Located in `src/main/resources/application.properties`:

spring.application.name=taskmanager
spring.data.mongodb.uri=mongodb://localhost:27017/kaiburr-task-db



The database is auto-created as needed in MongoDB named `kaiburr-task-db`.

---

## How to Build and Run

**Prerequisites**:
- Java 17+
- Maven (or use Maven Wrapper `mvnw`)
- MongoDB running at `localhost:27017`

**Building**:
- Linux/Mac: `./mvnw clean install`
- Windows: `.\mvnw.cmd clean install`
The executable `.jar` file is placed in `target/taskmanager-0.0.1-SNAPSHOT.jar`.

**Running**:
- For development:
    - Linux/Mac: `./mvnw spring-boot:run`
    - Windows: `.\mvnw.cmd spring-boot:run`
- For production:
    - `java -jar target/taskmanager-0.0.1-SNAPSHOT.jar`
By default, the API is served at http://localhost:8080.

---

## Screenshots

- ![Screenshot (184)](https://github.com/user-attachments/assets/25a203b7-b228-449a-95e7-b4841536ffbc)
- ![Screenshot (185)](https://github.com/user-attachments/assets/78e0032f-a7cc-459b-946b-9ca31165f8d9)
- ![Screenshot (186)](https://github.com/user-attachments/assets/9c9aab67-dd5d-4846-9ee5-5211e3f7e0a9)
- ![Screenshot (187)](https://github.com/user-attachments/assets/49aeb7a1-1b50-490a-b104-04ae383b6aa4)
- ![Screenshot (188)](https://github.com/user-attachments/assets/05dfd9e6-1e16-41e4-8937-929f9c904923)
- ![Screenshot (189)](https://github.com/user-attachments/assets/6e996697-c894-4b43-880e-76a68a567dac)
- ![Screenshot (190)](https://github.com/user-attachments/assets/d168a268-f9ee-487f-ac8e-66debaf03881)
- ![Screenshot (191)](https://github.com/user-attachments/assets/560a2c85-f0a3-4716-a1f8-065f4535db54)
- ![Screenshot (192)](https://github.com/user-attachments/assets/af412f10-64a0-4c4e-9b17-5f22cd2d76c3)
- ![Screenshot (193)](https://github.com/user-attachments/assets/03f8b60f-0b6b-4495-88a2-bba344cb4e5a)

---

For questions or contributions, contact Anandha Krishnan K.V.
