import { useEffect, useState } from 'react'
import api from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'
import SearchBar from '../components/SearchBar.jsx'
import VehicleCard from '../components/VehicleCard.jsx'

export default function Home() {
  const [vehicles, setVehicles] = useState([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [purchasingId, setPurchasingId] = useState(null)
  const { user } = useAuth()

  const loadAll = async () => {
    try {
      const { data } = await api.get('/vehicles')
      setVehicles(data)
    } catch {
      setError('Could not load vehicles. Is the backend running on port 8080?')
    }
  }

  useEffect(() => { loadAll() }, [])

  const handleSearch = async (filters) => {
    setError('')
    const params = Object.fromEntries(
      Object.entries(filters).filter(([, v]) => v !== '')
    )
    try {
      const { data } = await api.get('/vehicles/search', { params })
      setVehicles(data)
    } catch {
      setError('Search failed.')
    }
  }

  const handlePurchase = async (id) => {
    setMessage('')
    setError('')
    setPurchasingId(id)
    try {
      const { data } = await api.post(`/vehicles/${id}/purchase`)
      setVehicles((prev) => prev.map((v) => (v.id === id ? data : v)))
      setMessage(`Purchased ${data.make} ${data.model}. Remaining stock: ${data.quantity}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Purchase failed.')
    } finally {
      setPurchasingId(null)
    }
  }

  return (
    <>
      <section className="hero">
        <h1>The showroom floor</h1>
        <p className="muted">Browse the current inventory, filter by what you need, and drive one home.</p>
      </section>

      <SearchBar onSearch={handleSearch} onReset={loadAll} />

      {message && <div className="alert alert-ok">{message}</div>}
      {error && <div className="alert">{error}</div>}

      <div className="car-grid">
        {vehicles.map((v) => (
          <VehicleCard
            key={v.id}
            vehicle={v}
            canPurchase={!!user}
            onPurchase={handlePurchase}
            purchasing={purchasingId === v.id}
          />
        ))}
        {vehicles.length === 0 && !error && (
          <p className="muted">No vehicles match. Try clearing the filters, or ask an admin to add stock.</p>
        )}
      </div>
    </>
  )
}
