import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { data } = await api.post('/auth/login', form)
      login(data)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check your email and password.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-card">
      <div className="auth-icon" aria-hidden="true">🔑</div>
      <h1>Welcome back</h1>
      <p className="muted">Log in to browse and purchase vehicles.</p>
      {error && <div className="alert">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>
          Email
          <input name="email" type="email" value={form.email} onChange={handleChange} required />
        </label>
        <label>
          Password
          <input name="password" type="password" value={form.password} onChange={handleChange} required />
        </label>
        <button className="btn btn-solid btn-block" disabled={loading}>
          {loading ? 'Logging in…' : 'Log in'}
        </button>
      </form>
      <p className="muted">
        New here? <Link to="/register">Create an account</Link>
      </p>
    </div>
  )
}
