import { Component } from 'react'

export default class ErrorBoundary extends Component {
  state = { error: null }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    console.error('Unhandled UI error:', error, info)
  }

  render() {
    if (!this.state.error) return this.props.children

    return (
      <div className="page">
        <div className="auth-card" style={{ maxWidth: 520 }}>
          <div className="auth-icon" aria-hidden="true">⚠️</div>
          <h1>Something went wrong</h1>
          <p className="muted">
            {this.state.error?.message || 'An unexpected error occurred.'}
          </p>
          <button className="btn btn-solid" onClick={() => window.location.assign('/')}>
            Back to showroom
          </button>
        </div>
      </div>
    )
  }
}
