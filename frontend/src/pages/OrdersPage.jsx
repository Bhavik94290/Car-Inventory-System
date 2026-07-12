import { useEffect, useState } from 'react'
import api from '../api/client.js'

const priceFmt = (n) => Number(n).toLocaleString('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 0,
})

export default function OrdersPage() {
  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/orders/mine')
      .then(({ data }) => setOrders(data))
      .catch(() => setError('Could not load your orders.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <section className="hero">
        <h1>My orders</h1>
        <p className="muted">Your past purchases and payment status.</p>
      </section>

      {error && <div className="alert">{error}</div>}
      {!loading && !error && orders.length === 0 && <p className="muted">No orders yet.</p>}

      <div className="orders-list">
        {orders.map((order) => (
          <div className="order-card" key={order.id}>
            <div className="order-card-header">
              <span>Order {order.id}</span>
              <span className={`stock-pill ${order.status === 'PAID' ? 'stock-in' : 'stock-out'}`}>
                {order.status}
              </span>
            </div>
            <p className="muted small">{new Date(order.createdAt).toLocaleString()}</p>
            <ul className="order-items">
              {order.items.map((item) => (
                <li key={item.vehicleId}>
                  {item.quantity} × {item.make} {item.model} — {priceFmt(item.unitPrice * item.quantity)}
                </li>
              ))}
            </ul>
            <div className="order-card-total">Total: {priceFmt(order.totalAmount)}</div>
          </div>
        ))}
      </div>
    </>
  )
}
