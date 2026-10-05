package com.internal.tasktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Reject bad paging input up front. Previously page=0 reached subList() with a
        // negative index and surfaced as a 500.
        if (page < 1) {
            return badRequest("page must be 1 or greater");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return badRequest("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        // Parse status filter. An unknown value is a client error (400), not a server error (500).
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown status '" + status + "'. Allowed values: "
                        + Arrays.toString(TaskStatus.values()));
            }
        }

        // Normalize query input. % and _ are LIKE wildcards, so they are escaped to make the
        // search match them literally (the query declares '!' as the escape character).
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + escapeLike(query.toLowerCase(Locale.ROOT)) + "%";

        log.debug("searchTasks q=\"{}\" status={} page={} pageSize={}", query, normalizedStatus, page, pageSize);

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        // long arithmetic so a very large page number cannot overflow into a negative index
        long start = (long) (page - 1) * pageSize;
        List<Task> pageResults = Collections.emptyList();
        if (start < allResults.size()) {
            int end = (int) Math.min(start + pageSize, allResults.size());
            pageResults = allResults.subList((int) start, end);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }

    private static ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
