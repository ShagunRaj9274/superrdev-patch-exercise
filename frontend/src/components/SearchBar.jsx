import { useEffect, useState } from 'react';

const DEBOUNCE_MS = 300;

export default function SearchBar({ value, onChange }) {
  // The input keeps its own text so typing stays instant; the parent is only told
  // once the user pauses, which avoids one API call per keystroke.
  const [text, setText] = useState(value);

  useEffect(() => {
    if (text === value) return;
    const timer = setTimeout(() => onChange(text), DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [text, value, onChange]);

  return (
    <input
      type="text"
      className="search-input"
      placeholder="Search tasks..."
      aria-label="Search tasks"
      value={text}
      onChange={(e) => setText(e.target.value)}
    />
  );
}
