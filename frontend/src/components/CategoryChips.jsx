import { resolveCategoryStyle } from '../utils/categoryStyle.js'

export default function CategoryChips({ categories, active, onSelect }) {
  if (categories.length === 0) return null

  return (
    <div className="category-chips" role="tablist" aria-label="Quick filter by category">
      <button
        type="button"
        className={`chip ${active === '' ? 'chip-active' : ''}`}
        onClick={() => onSelect('')}
      >
        All
      </button>
      {categories.map((c) => {
        const style = resolveCategoryStyle(c)
        const isActive = active === c
        return (
          <button
            key={c}
            type="button"
            className={`chip ${isActive ? 'chip-active' : ''}`}
            style={isActive ? { background: style.accent, borderColor: style.accent } : { borderColor: style.accent, color: style.accent }}
            onClick={() => onSelect(c)}
          >
            <span aria-hidden="true">{style.emoji}</span> {c}
          </button>
        )
      })}
    </div>
  )
}
