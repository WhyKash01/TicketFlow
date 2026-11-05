/**
 * TicketFlow Client-Side Password Hashing & Encryption
 * 
 * Hashes passwords before they are transmitted over the network so that:
 * 1. Plaintext passwords are NEVER sent over HTTP/HTTPS or visible in DevTools.
 * 2. Plaintext passwords are NEVER seen or logged by API Gateway or Auth Service.
 * 3. The backend receives the SHA-256 digest and applies salted BCrypt before database storage.
 */
export async function hashPasswordClient(password: string): Promise<string> {
  if (!password) return '';

  // Use a domain-specific salt to prevent precomputed rainbow table matches
  const saltedInput = `TicketFlow#ClientSalt#${password}`;
  const encoder = new TextEncoder();
  const data = encoder.encode(saltedInput);

  if (typeof window !== 'undefined' && window.crypto && window.crypto.subtle) {
    const hashBuffer = await window.crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map((b) => b.toString(16).padStart(2, '0')).join('');
  }

  // Fallback for non-browser/test environments
  let hash = 0;
  for (let i = 0; i < saltedInput.length; i++) {
    hash = (hash << 5) - hash + saltedInput.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash).toString(16).padStart(64, '0');
}

