package com.internal.tasktracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Search tasks by term and optional status filter.
    // The OR is parenthesised on purpose: AND binds tighter than OR, so without the brackets
    // the archived and status conditions only apply to one side of the OR.
    // '!' is the LIKE escape character (see TaskController.escapeLike).
    @Query(value = "SELECT * FROM tasks WHERE archived = FALSE "
                 + "AND (LOWER(title) LIKE :term ESCAPE '!' OR LOWER(description) LIKE :term ESCAPE '!') "
                 + "AND (:status IS NULL OR status = :status) "
                 + "ORDER BY created_at DESC, id DESC",
           nativeQuery = true)
    List<Task> searchTasks(@Param("term") String term, @Param("status") String status);
}
