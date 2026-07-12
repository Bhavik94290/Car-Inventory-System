import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

const PASSWORD_RULES = [
  { key: 'length', label: 'At least 8 characters', test: (pw) => pw.length >= 8 },
  { key: 'upper', label: 'An uppercase letter', test: (pw) => /[A-Z]/.test(pw) },
  { key: 'lower', label: 'A lowercase letter', test: (pw) => /[a-z]/.test(pw) },
  { key: 'digit', label: 'A digit', test: (pw) => /\d/.test(pw) },
  { key: 'symbol', label: 'A symbol (e.g. !@#$%)', test: (pw) => /[^A-Za-z0-9\s]/.test(pw) },
]

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const passwordChecks = PASSWORD_RULES.map((rule) => ({ ...rule, met: rule.test(form.password) }))
  const passwordValid = passwordChecks.every((c) => c.met)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { data } = await api.post('/auth/register', form)
      login(data)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-card">
      <div className="auth-icon" aria-hidden="true">🚘</div>
      <h1>Create account</h1>
      <p className="muted">Register to start purchasing vehicles.</p>
      {error && <div className="alert">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>
          Name
          <input name="name" value={form.name} onChange={handleChange} required />
        </label>
        <label>
          Email
          <input name="email" type="email" value={form.email} onChange={handleChange} required />
        </label>
        <label>
          Password
          <input
            name="password"
            type="password"
            minLength={8}
            value={form.password}
            onChange={handleChange}
            required
          />
        </label>
        <ul className="password-checklist">
          {passwordChecks.map((c) => (
            <li key={c.key} className={c.met ? 'met' : ''}>
              <span aria-hidden="true">{c.met ? '✓' : '•'}</span> {c.label}
            </li>
          ))}
        </ul>
        <button className="btn btn-solid btn-block" disabled={loading || !passwordValid}>
          {loading ? 'Creating…' : 'Register'}
        </button>
      </form>
      <p className="muted">
        Already registered? <Link to="/login">Log in</Link>
      </p>
    </div>
  )
}
