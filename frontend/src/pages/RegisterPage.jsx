import { useState } from 'react'
import { useAuth } from '../auth/useAuth'
import { navigate } from '../app/router'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { IconBookOpen } from '../components/common/Icons'

const initialForm = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  password: '',
}

export function RegisterPage() {
  const { register } = useAuth()
  const [form, setForm] = useState(initialForm)
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
      await register(form)
      navigate('/catalog')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page fade-in">
      <div className="auth-card wide">
        <div className="auth-logo">
          <div className="auth-logo-icon">
            <IconBookOpen size={20} />
          </div>
          <span className="auth-logo-text">E-Library</span>
        </div>

        <h1 className="auth-title">Create your account</h1>
        <p className="auth-subtitle">Join E-Library and start reading today</p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          {error && <Alert type="error">{error}</Alert>}

          <div className="auth-form-grid">
            <div className="field">
              <label className="field-label" htmlFor="firstName">
                First name <span className="required">*</span>
              </label>
              <input
                id="firstName"
                name="firstName"
                className="input"
                placeholder="First name"
                value={form.firstName}
                onChange={updateField}
                required
                maxLength={50}
                autoFocus
              />
            </div>
            <div className="field">
              <label className="field-label" htmlFor="lastName">
                Last name <span className="required">*</span>
              </label>
              <input
                id="lastName"
                name="lastName"
                className="input"
                placeholder="Last name"
                value={form.lastName}
                onChange={updateField}
                required
                maxLength={50}
              />
            </div>
          </div>

          <div className="field">
            <label className="field-label" htmlFor="reg-email">
              Email address <span className="required">*</span>
            </label>
            <input
              id="reg-email"
              name="email"
              type="email"
              className="input"
              placeholder="you@example.com"
              value={form.email}
              onChange={updateField}
              required
              maxLength={100}
              autoComplete="email"
            />
          </div>

          <div className="field">
            <label className="field-label" htmlFor="phone">
              Phone number <span className="required">*</span>
            </label>
            <input
              id="phone"
              name="phone"
              type="tel"
              className="input"
              placeholder="9876543210 (Indian mobile)"
              value={form.phone}
              onChange={updateField}
              required
              pattern="[6-9][0-9]{9}"
              autoComplete="tel"
            />
          </div>

          <div className="field">
            <label className="field-label" htmlFor="reg-password">
              Password <span className="required">*</span>
            </label>
            <input
              id="reg-password"
              name="password"
              type="password"
              className="input"
              placeholder="Minimum 8 characters"
              value={form.password}
              onChange={updateField}
              required
              minLength={8}
              maxLength={100}
              autoComplete="new-password"
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={submitting}
          >
            {submitting ? (
              <>
                <Spinner /> Creating account…
              </>
            ) : (
              'Create account'
            )}
          </button>
        </form>

        <div className="auth-footer">
          Already have an account?{' '}
          <button
            type="button"
            className="auth-link"
            onClick={() => navigate('/login')}
          >
            Sign in
          </button>
        </div>
      </div>
    </div>
  )
}
