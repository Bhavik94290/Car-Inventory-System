import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'

const priceFmt = (n) => Number(n).toLocaleString('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 0,
})

export default function CartPage() {
  const { items, removeFromCart, updateQuantity, cartTotal } = useCart()
  const navigate = useNavigate()

  if (items.length === 0) {
    return (
      <section className="hero">
        <h1>Your cart</h1>
        <p className="muted">
          Your cart is empty. <Link to="/">Browse the showroom</Link> to add a vehicle.
        </p>
      </section>
    )
  }

  return (
    <>
      <section className="hero">
        <h1>Your cart</h1>
        <p className="muted">Review your selection before checkout.</p>
      </section>

      <div className="cart-list">
        {items.map((item) => (
          <div className="cart-row" key={item.vehicleId}>
            <div className="cart-row-info">
              <span className="car-category">{item.category}</span>
              <h3 className="car-title">{item.make} <span>{item.model}</span></h3>
              <p className="muted small">{priceFmt(item.price)} each</p>
            </div>
            <div className="qty-stepper">
              <button
                type="button"
                onClick={() => updateQuantity(item.vehicleId, item.quantity - 1)}
                disabled={item.quantity <= 1}
              >
                −
              </button>
              <span>{item.quantity}</span>
              <button
                type="button"
                onClick={() => updateQuantity(item.vehicleId, item.quantity + 1)}
                disabled={item.quantity >= item.stock}
              >
                +
              </button>
            </div>
            <div className="cart-row-subtotal">{priceFmt(item.price * item.quantity)}</div>
            <button className="btn btn-mini btn-danger" onClick={() => removeFromCart(item.vehicleId)}>
              Remove
            </button>
          </div>
        ))}
      </div>

      <div className="cart-summary">
        <span className="muted">Total</span>
        <span className="cart-total">{priceFmt(cartTotal)}</span>
      </div>

      <div className="load-more">
        <button className="btn btn-solid" onClick={() => navigate('/checkout')}>
          Proceed to Checkout
        </button>
      </div>
    </>
  )
}
