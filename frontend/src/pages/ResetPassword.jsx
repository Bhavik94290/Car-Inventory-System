import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import api from '../api/client.js'

const PASSWORD_RULES = [
  { key: 'length', label: 'At least 8 characters', test: (pw) => pw.length >= 8 },
  { key: 'upper', label: 'An uppercase letter', test: (pw) => /[A-Z]/.test(pw) },
  { key: 'lower', label: 'A lowercase letter', test: (pw) => /[a-z]/.test(pw) },
  { key: 'digit', label: 'A digit', test: (pw) => /\d/.test(pw) },
  { key: 'symbol', label: 'A symbol (e.g. !@#$%)', test: (pw) => /[^A-Za-z0-9\s]/.test(pw) },
]

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(false)
  const navigate = useNavigate()

  const passwordChecks = PASSWORD_RULES.map((rule) => ({ ...rule, met: rule.test(newPassword) }))
  const passwordValid = passwordChecks.every((c) => c.met)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/auth/reset-password', { token, newPassword })
      setDone(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reset your password.')
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <div className="auth-card">
        <div className="auth-icon" aria-hidden="true">⚠️</div>
        <h1>Invalid reset link</h1>
        <p className="muted">
          This link is missing its reset token. Request a new one from{' '}
          <Link to="/forgot-password">Forgot password</Link>.
        </p>
      </div>
    )
  }

  if (done) {
    return (
      <div className="auth-card">
        <div className="auth-icon" aria-hidden="true">✅</div>
        <h1>Password updated</h1>
        <p className="muted">You can now log in with your new password.</p>
        <button className="btn btn-solid btn-block" onClick={() => navigate('/login')}>
          Go to login
        </button>
      </div>
    )
  }

  return (
    <div className="auth-card">
      <div className="auth-icon" aria-hidden="true">🔑</div>
      <h1>Reset your password</h1>
      <p className="muted">Choose a new password for your account.</p>
      {error && <div className="alert">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>
          New password
          <input
            name="newPassword"
            type="password"
            minLength={8}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
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
          {loading ? 'Resetting…' : 'Reset password'}
        </button>
      </form>
      <p className="muted">
        <Link to="/login">Back to login</Link>
      </p>
    </div>
  )
}
