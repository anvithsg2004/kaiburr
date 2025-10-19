package com.example.kaiburr.service;

import com.example.kaiburr.model.Task;
import com.example.kaiburr.model.TaskExecution;
import com.example.kaiburr.repo.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository repo;
    private final CommandValidator validator;
    private final ShellExecutor executor;
    private final K8sPodExecutor k8sPodExecutor;

    public TaskService(TaskRepository repo, CommandValidator validator, ShellExecutor executor, K8sPodExecutor k8sPodExecutor) {
        this.repo = repo;
        this.validator = validator;
        this.executor = executor;
        this.k8sPodExecutor = k8sPodExecutor;
    }

    public List<Task> getAll() {
        return repo.findAll();
    }

    public Optional<Task> getById(String id) {
        return repo.findById(id);
    }

    public List<Task> searchByName(String q) {
        return repo.findByNameContainingIgnoreCase(q);
    }

    public Task upsert(Task t) {
        if (!StringUtils.hasText(t.getId())) throw new IllegalArgumentException("id required");
        if (!validator.isSafe(t.getCommand())) throw new IllegalArgumentException("unsafe command");
        if (t.getTaskExecutions() == null) t.setTaskExecutions(List.of());
        return repo.save(t);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    public TaskExecution execute(String id) throws Exception {
        Task task = repo.findById(id).orElseThrow();
        if (!validator.isSafe(task.getCommand())) throw new IllegalArgumentException("unsafe command");

        Instant start = Instant.now();
        String out = k8sPodExecutor.runInPod(task.getCommand());
        Instant end = Instant.now();

        TaskExecution te = new TaskExecution(start, end, out + "\n[executedIn=k8sPod]");
        task.getTaskExecutions().add(te);
        repo.save(task);
        return te;
    }
}
