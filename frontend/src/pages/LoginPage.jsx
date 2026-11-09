import { useState } from 'react'
import { useAuth } from '../auth/useAuth'
import { navigate } from '../app/router'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { IconBookOpen } from '../components/common/Icons'

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
            <IconBookOpen size={20} />
          </div>
          <span className="auth-logo-text">E-Library</span>
        </div>

        <h1 className="auth-title">Welcome back</h1>
        <p className="auth-subtitle">Sign in to your account to continue</p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          {error && <Alert type="error">{error}</Alert>}

          <div className="field">
            <label className="field-label" htmlFor="email">
              Email address <span className="required">*</span>
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
                <Spinner /> Signing in…
              </>
            ) : (
              'Sign in'
            )}
          </button>
        </form>

        <div className="auth-footer">
          Don&apos;t have an account?{' '}
          <button
            type="button"
            className="auth-link"
            onClick={() => navigate('/register')}
          >
            Create account
          </button>
        </div>
      </div>
    </div>
  )
}
