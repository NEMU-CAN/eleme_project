import { ref } from 'vue'

export type Theme = 'light' | 'dark'

function initialTheme(): Theme {
  const stored = localStorage.getItem('eleme_admin_theme')
  if (stored === 'light' || stored === 'dark') return stored
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export const theme = ref<Theme>(initialTheme())

export function applyTheme(next: Theme) {
  theme.value = next
  document.documentElement.dataset.theme = next
  localStorage.setItem('eleme_admin_theme', next)
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', next === 'dark' ? '#111513' : '#f4f7f5')
}

applyTheme(theme.value)
