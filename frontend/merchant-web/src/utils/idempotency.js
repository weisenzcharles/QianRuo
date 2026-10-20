let fallbackSequence = 0

/**
 * 生成幂等键。
 *
 * randomUUID 只在较新的浏览器和安全上下文中可用，先降级到
 * getRandomValues，保证商户端转单在旧浏览器中也能提交。
 *
 * @returns {string} UUID 格式或等价的唯一幂等键
 */
export function createIdempotencyKey() {
  const cryptoApi = globalThis.crypto
  if (typeof cryptoApi?.randomUUID === 'function') {
    return cryptoApi.randomUUID()
  }

  if (typeof cryptoApi?.getRandomValues === 'function') {
    const bytes = new Uint8Array(16)
    cryptoApi.getRandomValues(bytes)
    bytes[6] = (bytes[6] & 0x0f) | 0x40
    bytes[8] = (bytes[8] & 0x3f) | 0x80
    const hex = Array.from(bytes, byte => byte.toString(16).padStart(2, '0')).join('')
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`
  }

  fallbackSequence = (fallbackSequence + 1) % 1000000
  return `idempotency-${Date.now().toString(36)}-${fallbackSequence.toString(36)}`
}
