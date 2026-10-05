import { useState, useEffect } from 'react';
import { fetchTasks } from '../api';

export function useTasks(query, status, page, pageSize) {
  const [tasks, setTasks] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // One controller per request. When the inputs change (or the component unmounts)
    // the cleanup aborts the in-flight request, so a slow, older response can never
    // overwrite the results of a newer one.
    const controller = new AbortController();

    setLoading(true);
    setError(null);

    fetchTasks({ query, status, page, pageSize, signal: controller.signal })
      .then((data) => {
        setTasks(data.items);
        setTotal(data.total);
        setLoading(false);
      })
      .catch((err) => {
        // An aborted request has been superseded; the newer request owns the state.
        if (err.name === 'AbortError') return;
        setError(err.message);
        setLoading(false);
      });

    return () => controller.abort();
  }, [query, status, page, pageSize]);

  return { tasks, total, loading, error };
}
