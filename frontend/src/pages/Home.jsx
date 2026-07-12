import { useEffect, useRef, useState } from 'react'
import api from '../api/client.js'
import SearchBar from '../components/SearchBar.jsx'
import VehicleCard from '../components/VehicleCard.jsx'

const PAGE_SIZE = 12
const EMPTY_FILTERS = { make: '', model: '', category: '', minPrice: '', maxPrice: '' }
const DEBOUNCE_MS = 350

export default function Home() {
  const [filters, setFilters] = useState(EMPTY_FILTERS)
  const [inStockOnly, setInStockOnly] = useState(false)
  const [sort, setSort] = useState('createdAt,desc')
  const [page, setPage] = useState(0)
  const [vehicles, setVehicles] = useState([])
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const isFirstRun = useRef(true)

  const [sortBy, sortDir] = sort.split(',')

  useEffect(() => {
    api.get('/vehicles')
      .then(({ data }) => setCategories([...new Set(data.map((v) => v.category))].sort()))
      .catch(() => {})
  }, [])

  const fetchPage = async (pageToLoad, append) => {
    setError('')
    setLoading(true)
    const params = {
      ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== '')),
      inStockOnly,
      page: pageToLoad,
      size: PAGE_SIZE,
      sortBy,
      sortDir,
    }
    try {
      const { data } = await api.get('/vehicles/search', { params })
      // Defensive: tolerate an older backend build that returns a plain array
      // instead of a { content, totalElements, totalPages } page envelope.
      const content = Array.isArray(data) ? data : data.content ?? []
      const total = Array.isArray(data) ? content.length : data.totalElements ?? content.length
      const pages = Array.isArray(data) ? 1 : data.totalPages ?? 1
      setVehicles((prev) => (append ? [...prev, ...content] : content))
      setTotalElements(total)
      setTotalPages(pages)
    } catch {
      setError('Could not load vehicles. Is the backend running on port 8080?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    setPage(0)
    if (isFirstRun.current) {
      isFirstRun.current = false
      fetchPage(0, false)
      return
    }
    const timer = setTimeout(() => fetchPage(0, false), DEBOUNCE_MS)
    return () => clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters, inStockOnly, sort])

  const handleLoadMore = () => {
    const next = page + 1
    setPage(next)
    fetchPage(next, true)
  }

  const handleReset = () => {
    setFilters(EMPTY_FILTERS)
    setInStockOnly(false)
    setSort('createdAt,desc')
  }

  const hasActiveFilters = Object.values(filters).some((v) => v !== '') || inStockOnly

  return (
    <>
      <section className="hero">
        <h1>The showroom floor</h1>
        <p className="muted">Browse the current inventory, filter by what you need, and drive one home.</p>
      </section>

      <SearchBar
        filters={filters}
        onFiltersChange={setFilters}
        categories={categories}
        inStockOnly={inStockOnly}
        onInStockOnlyChange={setInStockOnly}
        sort={sort}
        onSortChange={setSort}
        onReset={handleReset}
        hasActiveFilters={hasActiveFilters}
      />

      <p className="muted small results-count">
        {loading && vehicles.length === 0
          ? 'Searching…'
          : `${totalElements} vehicle${totalElements === 1 ? '' : 's'} found`}
      </p>

      {error && <div className="alert">{error}</div>}

      <div className="car-grid">
        {vehicles.map((v) => (
          <VehicleCard key={v.id} vehicle={v} />
        ))}
        {vehicles.length === 0 && !error && !loading && (
          <p className="muted">No vehicles match. Try clearing the filters, or ask an admin to add stock.</p>
        )}
      </div>

      {page + 1 < totalPages && (
        <div className="load-more">
          <button className="btn btn-outline" onClick={handleLoadMore} disabled={loading}>
            {loading ? 'Loading…' : 'Load more'}
          </button>
        </div>
      )}
    </>
  )
}
