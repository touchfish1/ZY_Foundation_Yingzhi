export function statusType(status: string) {
  return status === 'paid' || status === 'success' || status === 'PAID' || status === 'SUCCESS' ? 'success'
    : status === 'pending' || status === 'PENDING' ? 'warning'
    : status === 'cancelled' || status === 'failed' || status === 'CANCELLED' || status === 'FAILED' ? 'error'
    : 'default'
}
