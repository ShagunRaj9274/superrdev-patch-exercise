-- H2-compatible task search query
-- Used by the Spring Data repository layer
--
-- Parameters:
--   :term   — search term wrapped in wildcards, e.g. '%api%'
--             (literal % and _ in user input are escaped with '!')
--   :status — status filter or NULL for all statuses

SELECT *
FROM tasks
WHERE archived = FALSE
  AND (LOWER(title) LIKE :term ESCAPE '!'
       OR LOWER(description) LIKE :term ESCAPE '!')
  AND (:status IS NULL OR status = :status)
ORDER BY created_at DESC, id DESC;
