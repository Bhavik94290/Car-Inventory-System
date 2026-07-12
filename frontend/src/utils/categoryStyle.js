export const CATEGORY_STYLES = {
  SUV: { gradient: ['#0f5e63', '#123f42'], car: '#eaf6f4', emoji: '🚙', accent: '#0f5e63' },
  SEDAN: { gradient: ['#1e3a5f', '#0f2038'], car: '#eef2f9', emoji: '🚗', accent: '#1e3a5f' },
  HATCHBACK: { gradient: ['#0e7490', '#083344'], car: '#e6fbff', emoji: '🚕', accent: '#0e7490' },
  TRUCK: { gradient: ['#b45309', '#7c3a0a'], car: '#fff3e0', emoji: '🚚', accent: '#b45309' },
  PICKUP: { gradient: ['#b45309', '#7c3a0a'], car: '#fff3e0', emoji: '🚚', accent: '#b45309' },
  VAN: { gradient: ['#6d28d9', '#3f1f7a'], car: '#f3ecff', emoji: '🚐', accent: '#6d28d9' },
  MINIVAN: { gradient: ['#6d28d9', '#3f1f7a'], car: '#f3ecff', emoji: '🚐', accent: '#6d28d9' },
  ELECTRIC: { gradient: ['#15803d', '#0b4023'], car: '#eafff1', emoji: '⚡', accent: '#15803d' },
  EV: { gradient: ['#15803d', '#0b4023'], car: '#eafff1', emoji: '⚡', accent: '#15803d' },
  COUPE: { gradient: ['#b3362a', '#701f18'], car: '#ffecea', emoji: '🏎️', accent: '#b3362a' },
  CONVERTIBLE: { gradient: ['#b3362a', '#701f18'], car: '#ffecea', emoji: '🏎️', accent: '#b3362a' },
  SPORTS: { gradient: ['#b3362a', '#701f18'], car: '#ffecea', emoji: '🏎️', accent: '#b3362a' },
  DEFAULT: { gradient: ['#5b6472', '#333941'], car: '#f3f4f5', emoji: '🚘', accent: '#5b6472' },
}

export function resolveCategoryStyle(category) {
  const key = (category || '').trim().toUpperCase()
  if (CATEGORY_STYLES[key]) return CATEGORY_STYLES[key]
  const found = Object.keys(CATEGORY_STYLES).find((k) => k !== 'DEFAULT' && key.includes(k))
  return CATEGORY_STYLES[found] || CATEGORY_STYLES.DEFAULT
}
