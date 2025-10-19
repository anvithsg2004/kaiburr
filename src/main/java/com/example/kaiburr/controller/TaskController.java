package com.example.kaiburr.controller;

import com.example.kaiburr.model.Task;
import com.example.kaiburr.model.TaskExecution;
import com.example.kaiburr.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(value = "id", required = false) String id) {
        if (id == null) return ResponseEntity.ok(service.getAll());
        return service.getById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found"));
    }

    @PutMapping
    public ResponseEntity<?> put(@Valid @RequestBody Task task) {
        try {
            return ResponseEntity.ok(service.upsert(task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (service.getById(id).isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("q") String q) {
        List<Task> r = service.searchByName(q);
        if (r.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
        return ResponseEntity.ok(r);
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<?> execute(@PathVariable String id) {
        try {
            TaskExecution te = service.execute(id);
            return ResponseEntity.ok(te);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Execution error");
        }
    }
}
