import { getStoredTokens, storeTokens, clearStoredTokens } from '../auth/tokenStorage'
import { getApiErrorMessage } from '../utils/apiError'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

async function parseResponse(response) {
  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type') ?? ''

  if (contentType.includes('application/json')) {
    return response.json()
  }

  if (contentType.includes('application/pdf') || contentType.startsWith('image/')) {
    return response.blob()
  }

  return response.text()
}

async function refreshAccessToken() {
  const { refreshToken } = getStoredTokens()

  if (!refreshToken) {
    return null
  }

  const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken }),
  })

  if (!response.ok) {
    clearStoredTokens()
    return null
  }

  const tokens = await response.json()
  storeTokens(tokens)
  return tokens.accessToken
}

async function request(path, options = {}, retryOnUnauthorized = true) {
  const { accessToken } = getStoredTokens()
  const headers = new Headers(options.headers)

  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (accessToken && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })

  if (response.status === 401 && retryOnUnauthorized) {
    const nextAccessToken = await refreshAccessToken()

    if (nextAccessToken) {
      return request(
        path,
        {
          ...options,
          headers: {
            ...Object.fromEntries(headers),
            Authorization: `Bearer ${nextAccessToken}`,
          },
        },
        false,
      )
    }
  }

  const data = await parseResponse(response)

  if (!response.ok) {
    const error = typeof data === 'object' && data !== null ? data : {
      status: response.status,
      statusText: response.statusText,
      message: data,
    }

    throw new Error(getApiErrorMessage(error))
  }

  return data
}

export const http = {
  get(path, options) {
    return request(path, { ...options, method: 'GET' })
  },
  post(path, body, options = {}) {
    return request(path, {
      ...options,
      method: 'POST',
      body: body instanceof FormData ? body : JSON.stringify(body),
    })
  },
  put(path, body, options = {}) {
    return request(path, {
      ...options,
      method: 'PUT',
      body: body instanceof FormData ? body : JSON.stringify(body),
    })
  },
  patch(path, options) {
    return request(path, { ...options, method: 'PATCH' })
  },
  delete(path, options) {
    return request(path, { ...options, method: 'DELETE' })
  },
}
