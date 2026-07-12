import { useState } from 'react'
import CarIllustration from './CarIllustration.jsx'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { resolveCategoryStyle } from '../utils/categoryStyle.js'

export default function VehicleCard({ vehicle }) {
  const [imgFailed, setImgFailed] = useState(false)
  const [qty, setQty] = useState(1)
  const [added, setAdded] = useState(false)
  const { addToCart } = useCart()
  const { isAdmin } = useAuth()
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
    <article className={`car-card ${outOfStock ? 'sold-out' : ''}`}>
      <div className="car-media">
        {showImage ? (
          <img
            src={vehicle.imageUrl}
            alt={`${vehicle.make} ${vehicle.model}`}
            onError={() => setImgFailed(true)}
          />
        ) : (
          <CarIllustration category={vehicle.category} />
        )}
        <span className={`stock-pill stock-pill-media ${outOfStock ? 'stock-out' : 'stock-in'}`}>
          {outOfStock ? 'Out of stock' : `${vehicle.quantity} in stock`}
        </span>
      </div>
      <div className="car-card-body">
        <span className="car-category" style={{ color: categoryStyle.accent, background: `${categoryStyle.accent}1a` }}>
          {categoryStyle.emoji} {vehicle.category}
        </span>
        <h3 className="car-title">{vehicle.make} <span>{vehicle.model}</span></h3>
        <div className="car-price">{priceFmt}</div>
        {isAdmin ? (
          <p className="muted small">Manage this vehicle from the Admin Dashboard.</p>
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
    </article>
  )
}
