import { useEffect, useState } from 'react'
import api from '../api/client.js'
import CarIllustration from '../components/CarIllustration.jsx'

const emptyForm = { make: '', model: '', category: '', price: '', quantity: '', imageUrl: '' }
const shortId = (id) => (id && id.length > 16 ? `${id.slice(0, 14)}…` : id)

export default function AdminDashboard() {
  const [vehicles, setVehicles] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [uploading, setUploading] = useState(false)

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
    setForm({ make: v.make, model: v.model, category: v.category, price: v.price, quantity: v.quantity, imageUrl: v.imageUrl || '' })
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

  const handleImageUpload = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    setError('')
    setUploading(true)
    const body = new FormData()
    body.append('file', file)
    try {
      const { data } = await api.post('/vehicles/upload-image', body, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      setForm((f) => ({ ...f, imageUrl: data.imageUrl }))
      notify('Image uploaded.')
    } catch (err) {
      fail(err, 'Image upload failed.')
    } finally {
      setUploading(false)
      e.target.value = ''
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
          <input name="imageUrl" placeholder="Image URL (optional)" value={form.imageUrl} onChange={handleChange} />
        </div>
        <div className="admin-form-image-row">
          <label className="btn btn-outline btn-mini upload-btn">
            {uploading ? 'Uploading…' : '📷 Upload photo'}
            <input type="file" accept="image/*" onChange={handleImageUpload} disabled={uploading} hidden />
          </label>
          {form.imageUrl && (
            <div className="admin-image-preview">
              <img src={form.imageUrl} alt="Preview" onError={(e) => { e.currentTarget.style.display = 'none' }} />
              <button type="button" className="btn btn-mini btn-danger" onClick={() => setForm((f) => ({ ...f, imageUrl: '' }))}>
                Remove
              </button>
            </div>
          )}
        </div>
        <div className="admin-form-actions">
          <button className="btn btn-solid" type="submit">{editingId ? 'Save changes' : 'Add vehicle'}</button>
          {editingId && <button className="btn btn-outline" type="button" onClick={cancelEdit}>Cancel</button>}
        </div>
      </form>

      <table className="inventory-table">
        <thead>
          <tr>
            <th>Photo</th><th>ID</th><th>Make</th><th>Model</th><th>Category</th><th>Price</th><th>Qty</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {vehicles.map((v) => (
            <tr key={v.id} className={v.quantity === 0 ? 'row-out' : ''}>
              <td className="table-thumb">
                {v.imageUrl ? (
                  <img src={v.imageUrl} alt="" onError={(e) => { e.currentTarget.style.display = 'none' }} />
                ) : (
                  <CarIllustration category={v.category} className="table-thumb-illustration" />
                )}
              </td>
              <td title={v.id}>{shortId(v.id)}</td>
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
