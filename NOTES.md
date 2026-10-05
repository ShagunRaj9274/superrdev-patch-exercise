# NOTES

## Summary of changes (in priority order)

1. **SQL precedence bug** (`TaskRepository`, `db/queries`, Oracle package): `AND` binds tighter than `OR`, so archived tasks leaked and the status filter was ignored for title matches. Parenthesised the `OR`.
2. **Stale results** (`useTasks.js`): overlapping requests resolved out of order. Superseded requests are now aborted.
3. **Artificial delay** (`TaskController`): removed a `Thread.sleep` of up to 1s.
4. **Stuck loading / sticky error** (`useTasks.js`): `loading` was never cleared on failure and `error` never cleared on success.
5. **Page not reset** (`App.jsx`): changing search or filter on page 4 showed "No tasks found".
6. **500s for bad input** (`TaskController`): unknown status, `page=0`, oversized `pageSize` now return 400 with a message.
7. Smaller: debounced search, `%`/`_` escaped in LIKE, `id` tiebreaker in ORDER BY, SLF4J logging, Oracle `v_term` overflow and paging guards.

## Not changed, and why

- **In-memory pagination**: correct at 50 rows; DB paging changes the repository contract and deserves tests first.
- `status` as String, H2 console / `show-sql` enabled, hardcoded CORS origin: design choices, outside a patch.
- No tests added: out of timebox.

## Biggest remaining risk

Every request loads all matching rows into memory and runs `LOWER(col) LIKE '%term%'`, which cannot use an index, so it degrades linearly with table size. The same query also lives in three places with no tests, so the precedence bug can silently return.

## Assumptions

- Archived tasks must never appear in search.
- Invalid parameters should be rejected (400) rather than silently corrected.
- The Oracle package was not executed; reviewed by reading only.

## Tools / AI

Used Claude to review the codebase, reproduce the bugs and draft the fixes. I re-ran each reproduction myself (curl and browser), read every diff, and kept the change set small.
