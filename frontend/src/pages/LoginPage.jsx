import { useState } from 'react'
import { useAuth } from '../auth/useAuth'
import { navigate } from '../app/router'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { BrandLogo } from '../components/common/Icons'

export function LoginPage() {
  const { login } = useAuth()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function updateField(e) {
    setForm((cur) => ({ ...cur, [e.target.name]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(form)
      navigate('/catalog')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page fade-in">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="auth-logo-icon">
            <BrandLogo size={20} />
          </div>
          <div>
            <span className="auth-logo-text">
              Vellum<sup className="brand-tm">®</sup>
            </span>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 500 }}>
              Executive Digital Library
            </div>
          </div>
        </div>

        <h1 className="auth-title">Sign in to your workspace</h1>
        <p className="auth-subtitle">Access curated publications, research, and your personal shelf</p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          {error && <Alert type="error">{error}</Alert>}

          <div className="field">
            <label className="field-label" htmlFor="email">
              Work or personal email <span className="required">*</span>
            </label>
            <input
              id="email"
              name="email"
              type="email"
              className="input"
              placeholder="name@company.com"
              value={form.email}
              onChange={updateField}
              required
              autoComplete="email"
              autoFocus
            />
          </div>

          <div className="field">
            <label className="field-label" htmlFor="password">
              Password <span className="required">*</span>
            </label>
            <input
              id="password"
              name="password"
              type="password"
              className="input"
              placeholder="Enter your password"
              value={form.password}
              onChange={updateField}
              required
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={submitting}
          >
            {submitting ? (
              <>
                <Spinner /> Authenticating…
              </>
            ) : (
              'Sign in'
            )}
          </button>
        </form>

        <div className="auth-footer">
          New to Vellum®?{' '}
          <button
            type="button"
            className="auth-link"
            onClick={() => navigate('/register')}
          >
            Create an account
          </button>
        </div>
      </div>
    </div>
  )
}
