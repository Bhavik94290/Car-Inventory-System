import { useEffect, useState } from 'react'
import api from '../api/client.js'

const emptyForm = { make: '', model: '', category: '', price: '', quantity: '' }

export default function AdminDashboard() {
  const [vehicles, setVehicles] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const load = async () => {
    try {
      const { data } = await api.get('/vehicles')
      setVehicles(data)
    } catch {
      setError('Could not load vehicles.')
    }
  }

  useEffect(() => { load() }, [])

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const notify = (msg) => { setMessage(msg); setError('') }
  const fail = (err, fallback) => {
    setError(err.response?.data?.message || fallback)
    setMessage('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const payload = { ...form, price: Number(form.price), quantity: Number(form.quantity) }
    try {
      if (editingId) {
        await api.put(`/vehicles/${editingId}`, payload)
        notify('Vehicle updated.')
      } else {
        await api.post('/vehicles', payload)
        notify('Vehicle added.')
      }
      setForm(emptyForm)
      setEditingId(null)
      load()
    } catch (err) {
      fail(err, 'Save failed.')
    }
  }

  const startEdit = (v) => {
    setEditingId(v.id)
    setForm({ make: v.make, model: v.model, category: v.category, price: v.price, quantity: v.quantity })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const cancelEdit = () => {
    setEditingId(null)
    setForm(emptyForm)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this vehicle?')) return
    try {
      await api.delete(`/vehicles/${id}`)
      notify('Vehicle deleted.')
      load()
    } catch (err) {
      fail(err, 'Delete failed.')
    }
  }

  const handleRestock = async (id) => {
    const amount = window.prompt('Restock amount:', '1')
    if (!amount) return
    try {
      const { data } = await api.post(`/vehicles/${id}/restock`, { amount: Number(amount) })
      notify(`Restocked ${data.make} ${data.model}. New quantity: ${data.quantity}`)
      load()
    } catch (err) {
      fail(err, 'Restock failed.')
    }
  }

  return (
    <>
      <section className="hero">
        <h1>Admin dashboard</h1>
        <p className="muted">Add, update, delete and restock inventory.</p>
      </section>

      {message && <div className="alert alert-ok">{message}</div>}
      {error && <div className="alert">{error}</div>}

      <form className="admin-form" onSubmit={handleSubmit}>
        <h2>{editingId ? `Edit vehicle #${editingId}` : 'Add vehicle'}</h2>
        <div className="admin-form-grid">
          <input name="make" placeholder="Make" value={form.make} onChange={handleChange} required />
          <input name="model" placeholder="Model" value={form.model} onChange={handleChange} required />
          <input name="category" placeholder="Category" value={form.category} onChange={handleChange} required />
          <input name="price" type="number" min="1" placeholder="Price" value={form.price} onChange={handleChange} required />
          <input name="quantity" type="number" min="0" placeholder="Quantity" value={form.quantity} onChange={handleChange} required />
        </div>
        <div className="admin-form-actions">
          <button className="btn btn-solid" type="submit">{editingId ? 'Save changes' : 'Add vehicle'}</button>
          {editingId && <button className="btn btn-outline" type="button" onClick={cancelEdit}>Cancel</button>}
        </div>
      </form>

      <table className="inventory-table">
        <thead>
          <tr>
            <th>ID</th><th>Make</th><th>Model</th><th>Category</th><th>Price</th><th>Qty</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {vehicles.map((v) => (
            <tr key={v.id} className={v.quantity === 0 ? 'row-out' : ''}>
              <td>{v.id}</td>
              <td>{v.make}</td>
              <td>{v.model}</td>
              <td>{v.category}</td>
              <td>{Number(v.price).toLocaleString('en-IN')}</td>
              <td>{v.quantity === 0 ? <strong>0 · out</strong> : v.quantity}</td>
              <td className="row-actions">
                <button className="btn btn-mini" onClick={() => startEdit(v)}>Edit</button>
                <button className="btn btn-mini" onClick={() => handleRestock(v.id)}>Restock</button>
                <button className="btn btn-mini btn-danger" onClick={() => handleDelete(v.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  )
}
