function decodeBase64Url(value) {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
  return atob(padded)
}

export function decodeJwt(token) {
  if (!token) {
    return null
  }

  try {
    const [, payload] = token.split('.')
    return JSON.parse(decodeBase64Url(payload))
  } catch {
    return null
  }
}

export function isJwtExpired(token) {
  const payload = decodeJwt(token)

  if (!payload?.exp) {
    return false
  }

  return payload.exp * 1000 <= Date.now()
}
