const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: 'Newest listed' },
  { value: 'price,asc', label: 'Price: Low to High' },
  { value: 'price,desc', label: 'Price: High to Low' },
  { value: 'quantity,desc', label: 'Most in stock' },
  { value: 'make,asc', label: 'Make: A–Z' },
]

export default function SearchBar({
  filters, onFiltersChange, categories,
  inStockOnly, onInStockOnlyChange,
  sort, onSortChange, onReset, hasActiveFilters,
}) {
  const handleChange = (e) => onFiltersChange({ ...filters, [e.target.name]: e.target.value })

  return (
    <div className="search-bar" role="search">
      <input name="make" placeholder="Make (e.g. Toyota)" value={filters.make} onChange={handleChange} />
      <input name="model" placeholder="Model (e.g. Fortuner)" value={filters.model} onChange={handleChange} />
      <select name="category" value={filters.category} onChange={handleChange}>
        <option value="">All categories</option>
        {categories.map((c) => (
          <option key={c} value={c}>{c}</option>
        ))}
      </select>
      <input name="minPrice" type="number" min="0" placeholder="Min price" value={filters.minPrice} onChange={handleChange} />
      <input name="maxPrice" type="number" min="0" placeholder="Max price" value={filters.maxPrice} onChange={handleChange} />
      <select value={sort} onChange={(e) => onSortChange(e.target.value)} aria-label="Sort by">
        {SORT_OPTIONS.map((opt) => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>
      <label className="checkbox-inline">
        <input
          type="checkbox"
          checked={inStockOnly}
          onChange={(e) => onInStockOnlyChange(e.target.checked)}
        />
        In stock only
      </label>
      {hasActiveFilters && (
        <button className="btn btn-outline btn-mini" type="button" onClick={onReset}>
          Clear filters
        </button>
      )}
    </div>
  )
}
