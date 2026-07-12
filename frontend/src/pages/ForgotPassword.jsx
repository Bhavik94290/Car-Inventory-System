import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client.js'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { data } = await api.post('/auth/forgot-password', { email })
      setResult(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (result) {
    return (
      <div className="auth-card">
        <div className="auth-icon" aria-hidden="true">📬</div>
        <h1>Check your email</h1>
        <p className="muted">{result.message}</p>
        {result.resetToken && (
          <div className="alert alert-ok">
            No email server is configured for this demo, so here's your reset
            link instead of an email:
            <br />
            <Link to={`/reset-password?token=${result.resetToken}`}>
              Reset your password
            </Link>
          </div>
        )}
        <p className="muted">
          <Link to="/login">Back to login</Link>
        </p>
      </div>
    )
  }

  return (
    <div className="auth-card">
      <div className="auth-icon" aria-hidden="true">🔒</div>
      <h1>Forgot your password?</h1>
      <p className="muted">Enter your account email and we'll send you a reset link.</p>
      {error && <div className="alert">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>
          Email
          <input
            name="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>
        <button className="btn btn-solid btn-block" disabled={loading}>
          {loading ? 'Sending…' : 'Send reset link'}
        </button>
      </form>
      <p className="muted">
        <Link to="/login">Back to login</Link>
      </p>
    </div>
  )
}
