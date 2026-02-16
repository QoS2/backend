import { useState, useEffect } from 'react';

const STORAGE_KEY = 'admin-theme-v2';
const DATA_THEME = 'data-theme';

function getInitial(): boolean {
  if (typeof document === 'undefined') return false;
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'light') return false;
  if (stored === 'dark') return true;
  return false;
}

export function useDarkMode(): [boolean, React.Dispatch<React.SetStateAction<boolean>>] {
  const [darkMode, setDarkMode] = useState(getInitial);

  useEffect(() => {
    document.documentElement.setAttribute(
      DATA_THEME,
      darkMode ? 'dark' : 'light'
    );
    localStorage.setItem(STORAGE_KEY, darkMode ? 'dark' : 'light');
  }, [darkMode]);

  return [darkMode, setDarkMode];
}
