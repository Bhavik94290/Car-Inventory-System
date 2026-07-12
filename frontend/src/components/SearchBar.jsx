import { useState } from 'react'

export default function SearchBar({ onSearch, onReset }) {
  const [filters, setFilters] = useState({
    make: '', model: '', category: '', minPrice: '', maxPrice: '',
  })

  const handleChange = (e) => setFilters({ ...filters, [e.target.name]: e.target.value })

  const handleSubmit = (e) => {
    e.preventDefault()
    onSearch(filters)
  }

  const handleReset = () => {
    setFilters({ make: '', model: '', category: '', minPrice: '', maxPrice: '' })
    onReset()
  }

  return (
    <form className="search-bar" onSubmit={handleSubmit}>
      <input name="make" placeholder="Make (e.g. Toyota)" value={filters.make} onChange={handleChange} />
      <input name="model" placeholder="Model (e.g. Fortuner)" value={filters.model} onChange={handleChange} />
      <input name="category" placeholder="Category (e.g. SUV)" value={filters.category} onChange={handleChange} />
      <input name="minPrice" type="number" min="0" placeholder="Min price" value={filters.minPrice} onChange={handleChange} />
      <input name="maxPrice" type="number" min="0" placeholder="Max price" value={filters.maxPrice} onChange={handleChange} />
      <button className="btn btn-solid" type="submit">Search</button>
      <button className="btn btn-outline" type="button" onClick={handleReset}>Reset</button>
    </form>
  )
}
