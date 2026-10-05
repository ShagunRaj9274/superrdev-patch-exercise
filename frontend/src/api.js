const API_BASE = '/api';

export async function fetchTasks({ query = '', status = '', page = 1, pageSize = 10, signal }) {
  const params = new URLSearchParams();
  if (query) params.set('q', query);
  if (status) params.set('status', status);
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));

  const response = await fetch(`${API_BASE}/tasks?${params.toString()}`, { signal });

  if (!response.ok) {
    // The API sends { error: "..." } for 400s; fall back to the status code otherwise.
    const body = await response.json().catch(() => null);
    throw new Error(body?.error || `Request failed: ${response.status}`);
  }

  return response.json();
}
