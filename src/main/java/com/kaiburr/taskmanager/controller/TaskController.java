package com.kaiburr.taskmanager.controller;

import com.kaiburr.taskmanager.model.Task;
import com.kaiburr.taskmanager.model.TaskExecution;
import com.kaiburr.taskmanager.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    // GET /tasks
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // GET /tasks/{id}
    @GetMapping("/{id}")
    // FIX: Added ("id") to @PathVariable
    public ResponseEntity<Task> getTaskById(@PathVariable("id") String id) {
        Optional<Task> taskOpt = taskRepository.findById(id);

        if (taskOpt.isPresent()) {
            return ResponseEntity.ok(taskOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /tasks/search?name=...
    @GetMapping("/search")
    // FIX: Added ("name") to @RequestParam
    public ResponseEntity<List<Task>> findTasksByName(@RequestParam("name") String name) {
        List<Task> tasks = taskRepository.findByNameContainingIgnoreCase(name);
        
        if (tasks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tasks);
    }

    // PUT /tasks
    @PutMapping
    public ResponseEntity<Task> createOrUpdateTask(@RequestBody Task task) {
        if (isUnsafe(task.getCommand())) {
            return ResponseEntity.badRequest().body(null); 
        }
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    private boolean isUnsafe(String command) {
        if (command == null) return false;
        String lowerCaseCommand = command.toLowerCase();
        String[] unsafe = {"rm ", "mv ", "sudo ", "shutdown", "reboot", "dd "}; 
        for (String unsafeCmd : unsafe) {
            if (lowerCaseCommand.contains(unsafeCmd)) {
                return true;
            }
        }
        return false;
    }

    // DELETE /tasks/{id}
    @DeleteMapping("/{id}")
    // FIX: Added ("id") to @PathVariable
    public ResponseEntity<Void> deleteTask(@PathVariable("id") String id) {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /tasks/{id}/execute
    @PutMapping("/{id}/execute")
    // FIX: Added ("id") to @PathVariable
    public ResponseEntity<Task> executeTask(@PathVariable("id") String id) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOpt.get();
        TaskExecution execution = new TaskExecution();
        execution.setStartTime(new Date());

        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.redirectErrorStream(true); // Merge stdout and stderr

            // FIX: Added OS check for Windows vs Linux/Mac
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                builder.command("cmd.exe", "/c", task.getCommand());
            } else {
                builder.command("sh", "-c", task.getCommand());
            }
            
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            if (!process.waitFor(1, TimeUnit.MINUTES)) { // 1-minute timeout
                process.destroyForcibly();
                output.append("\n...Error: Task timed out after 1 minute...");
            }
            
            execution.setOutput(output.toString().trim());

        } catch (Exception e) {
            execution.setOutput("Execution failed: " + e.getMessage());
            e.printStackTrace(); 
        }

        execution.setEndTime(new Date());
        task.getTaskExecutions().add(execution);
        taskRepository.save(task);
        
        return ResponseEntity.ok(task);
    }
}