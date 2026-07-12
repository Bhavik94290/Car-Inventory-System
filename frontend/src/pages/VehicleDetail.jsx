import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client.js'
import CarIllustration from '../components/CarIllustration.jsx'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { resolveCategoryStyle } from '../utils/categoryStyle.js'

export default function VehicleDetail() {
  const { id } = useParams()
  const [vehicle, setVehicle] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [imgFailed, setImgFailed] = useState(false)
  const [qty, setQty] = useState(1)
  const [added, setAdded] = useState(false)
  const { addToCart } = useCart()
  const { user, isAdmin } = useAuth()

  useEffect(() => {
    setLoading(true)
    setError('')
    setImgFailed(false)
    setQty(1)
    api.get(`/vehicles/${id}`)
      .then(({ data }) => setVehicle(data))
      .catch(() => setError('Vehicle not found.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return <p className="muted">Loading…</p>
  }

  if (error || !vehicle) {
    return (
      <section>
        <div className="alert">{error || 'Vehicle not found.'}</div>
        <Link to="/">← Back to showroom</Link>
      </section>
    )
  }

  const categoryStyle = resolveCategoryStyle(vehicle.category)
  const outOfStock = vehicle.quantity === 0
  const showImage = !!vehicle.imageUrl && !imgFailed
  const priceFmt = Number(vehicle.price).toLocaleString('en-IN', {
    style: 'currency', currency: 'INR', maximumFractionDigits: 0,
  })

  const handleAdd = () => {
    addToCart(vehicle, qty)
    setAdded(true)
    setTimeout(() => setAdded(false), 1500)
  }

  return (
    <>
      <Link to="/" className="back-link">← Back to showroom</Link>

      <div className="vehicle-detail">
        <div className="vehicle-detail-media">
          {showImage ? (
            <img
              src={vehicle.imageUrl}
              alt={`${vehicle.make} ${vehicle.model}`}
              onError={() => setImgFailed(true)}
            />
          ) : (
            <CarIllustration category={vehicle.category} />
          )}
        </div>

        <div className="vehicle-detail-info">
          <span className="car-category" style={{ color: categoryStyle.accent, background: `${categoryStyle.accent}1a` }}>
            {categoryStyle.emoji} {vehicle.category}
          </span>
          <h1 className="car-title">{vehicle.make} <span>{vehicle.model}</span></h1>
          <div className="car-price">{priceFmt}</div>
          <span className={`stock-pill ${outOfStock ? 'stock-out' : 'stock-in'}`}>
            {outOfStock ? 'Out of stock' : `${vehicle.quantity} in stock`}
          </span>

          <ul className="vehicle-detail-specs">
            <li><span>Make</span><strong>{vehicle.make}</strong></li>
            <li><span>Model</span><strong>{vehicle.model}</strong></li>
            <li><span>Category</span><strong>{vehicle.category}</strong></li>
            <li><span>Available units</span><strong>{vehicle.quantity}</strong></li>
          </ul>

          {isAdmin ? (
            <p className="muted small">Manage this vehicle from the Admin Dashboard.</p>
          ) : !user ? (
            <p className="muted small"><Link to="/login">Log in</Link> to add this to your cart.</p>
          ) : outOfStock ? (
            <p className="muted small">Currently unavailable</p>
          ) : (
            <div className="cart-controls">
              <div className="qty-stepper">
                <button type="button" onClick={() => setQty((q) => Math.max(1, q - 1))} disabled={qty <= 1}>
                  −
                </button>
                <span>{qty}</span>
                <button
                  type="button"
                  onClick={() => setQty((q) => Math.min(vehicle.quantity, q + 1))}
                  disabled={qty >= vehicle.quantity}
                >
                  +
                </button>
              </div>
              <button className="btn btn-cta btn-block" onClick={handleAdd}>
                {added ? 'Added ✓' : 'Add to Cart'}
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  )
}
