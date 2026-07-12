import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/client.js'

const PASSWORD_RULES = [
  { key: 'length', label: 'At least 8 characters', test: (pw) => pw.length >= 8 },
  { key: 'upper', label: 'An uppercase letter', test: (pw) => /[A-Z]/.test(pw) },
  { key: 'lower', label: 'A lowercase letter', test: (pw) => /[a-z]/.test(pw) },
  { key: 'digit', label: 'A digit', test: (pw) => /\d/.test(pw) },
  { key: 'symbol', label: 'A symbol (e.g. !@#$%)', test: (pw) => /[^A-Za-z0-9\s]/.test(pw) },
]

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [stage, setStage] = useState('email') // 'email' | 'reset' | 'done'
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const passwordChecks = PASSWORD_RULES.map((rule) => ({ ...rule, met: rule.test(newPassword) }))
  const passwordValid = passwordChecks.every((c) => c.met)

  const handleSendOtp = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/auth/forgot-password', { email })
      setStage('reset')
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleReset = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/auth/reset-password', { email, otp, newPassword })
      setStage('done')
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reset your password.')
    } finally {
      setLoading(false)
    }
  }

  if (stage === 'done') {
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

  if (stage === 'reset') {
    return (
      <div className="auth-card">
        <div className="auth-icon" aria-hidden="true">🔑</div>
        <h1>Enter your code</h1>
        <p className="muted">We emailed a 6-digit code to {email}. Enter it below with your new password.</p>
        {error && <div className="alert">{error}</div>}
        <form onSubmit={handleReset}>
          <label>
            6-digit code
            <input
              name="otp"
              inputMode="numeric"
              autoComplete="one-time-code"
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
              required
            />
          </label>
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
          <button className="btn btn-solid btn-block" disabled={loading || otp.length !== 6 || !passwordValid}>
            {loading ? 'Resetting…' : 'Reset password'}
          </button>
        </form>
        <p className="muted">
          <Link to="/forgot-password" onClick={(e) => { e.preventDefault(); setStage('email'); setError('') }}>
            Use a different email
          </Link>
        </p>
      </div>
    )
  }

  return (
    <div className="auth-card">
      <div className="auth-icon" aria-hidden="true">🔒</div>
      <h1>Forgot your password?</h1>
      <p className="muted">Enter your account email and we'll send you a 6-digit code.</p>
      {error && <div className="alert">{error}</div>}
      <form onSubmit={handleSendOtp}>
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
          {loading ? 'Sending…' : 'Send code'}
        </button>
      </form>
      <p className="muted">
        <Link to="/login">Back to login</Link>
      </p>
    </div>
  )
}
