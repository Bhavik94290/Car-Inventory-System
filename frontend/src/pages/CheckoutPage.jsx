import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import api from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'

const priceFmt = (n) => Number(n).toLocaleString('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 0,
})

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true)
      return
    }
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

export default function CheckoutPage() {
  const { user } = useAuth()
  const { items, cartTotal, clearCart } = useCart()
  const [paying, setPaying] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(null)
  const navigate = useNavigate()

  if (!user) return <Navigate to="/login" replace />
  if (items.length === 0 && !success) return <Navigate to="/cart" replace />

  const handlePay = async () => {
    setError('')
    setPaying(true)
    try {
      const scriptLoaded = await loadRazorpayScript()
      if (!scriptLoaded) {
        setError('Could not load the Razorpay checkout script. Check your internet connection and try again.')
        setPaying(false)
        return
      }

      const { data: checkout } = await api.post('/orders/checkout', {
        items: items.map((i) => ({ vehicleId: i.vehicleId, quantity: i.quantity })),
      })

      const rzp = new window.Razorpay({
        key: checkout.keyId,
        amount: checkout.amount,
        currency: checkout.currency,
        order_id: checkout.razorpayOrderId,
        name: 'AI·KATA Car Dealership',
        description: `${items.length} vehicle${items.length === 1 ? '' : 's'}`,
        prefill: { name: user.name, email: user.email },
        theme: { color: '#0f5e63' },
        handler: async (response) => {
          try {
            const { data: order } = await api.post('/orders/verify', {
              orderId: checkout.orderId,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            clearCart()
            setSuccess(order)
          } catch (err) {
            setError(
              err.response?.data?.message ||
              `Payment succeeded but verification failed. Save this payment ID and contact support: ${response.razorpay_payment_id}`
            )
          } finally {
            setPaying(false)
          }
        },
        modal: {
          ondismiss: () => setPaying(false),
        },
      })

      rzp.on('payment.failed', (resp) => {
        setError(resp.error?.description || 'Payment failed.')
        setPaying(false)
      })

      rzp.open()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not start checkout.')
      setPaying(false)
    }
  }

  if (success) {
    return (
      <div className="auth-card" style={{ maxWidth: 480 }}>
        <div className="auth-icon" aria-hidden="true">✅</div>
        <h1>Payment successful</h1>
        <p className="muted">Order {success.id} confirmed — {priceFmt(success.totalAmount)} paid.</p>
        <button className="btn btn-solid btn-block" onClick={() => navigate('/orders')}>
          View my orders
        </button>
        <button
          className="btn btn-outline btn-block"
          style={{ marginTop: 10 }}
          onClick={() => navigate('/')}
        >
          Back to showroom
        </button>
      </div>
    )
  }

  return (
    <>
      <section className="hero">
        <h1>Checkout</h1>
        <p className="muted">Review your order and pay securely with Razorpay.</p>
      </section>

      {error && <div className="alert">{error}</div>}

      <div className="cart-list">
        {items.map((item) => (
          <div className="cart-row" key={item.vehicleId}>
            <div className="cart-row-info">
              <span className="car-category">{item.category}</span>
              <h3 className="car-title">{item.make} <span>{item.model}</span></h3>
              <p className="muted small">Qty {item.quantity} × {priceFmt(item.price)}</p>
            </div>
            <div className="cart-row-subtotal">{priceFmt(item.price * item.quantity)}</div>
          </div>
        ))}
      </div>

      <div className="cart-summary">
        <span className="muted">Total</span>
        <span className="cart-total">{priceFmt(cartTotal)}</span>
      </div>

      <div className="load-more">
        <button className="btn btn-solid" onClick={handlePay} disabled={paying}>
          {paying ? 'Processing…' : `Pay ${priceFmt(cartTotal)} with Razorpay`}
        </button>
      </div>
    </>
  )
}
