export function getApiErrorMessage(error) {
  if (!error) {
    return 'Something went wrong.'
  }

  if (Array.isArray(error.error_message) && error.error_message.length > 0) {
    return error.error_message.join(' ')
  }

  if (error.message) {
    return error.message
  }

  if (error.status && error.statusText) {
    return `${error.status} ${error.statusText}`
  }

  return 'Something went wrong.'
}
