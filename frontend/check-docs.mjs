/* global process, fetch, console, AbortSignal */
import assert from 'node:assert/strict';

const origin = process.env.DOCS_ORIGIN || 'http://localhost:5173';
const endpoints = {
  auth: ['/openapi.json', ['/login']],
  user: ['/v3/api-docs', ['/api/users/me']],
  balance: ['/v3/api-docs', ['/api/balances/me', '/api/balances/deduct']],
  tuition: ['/v3/api-docs', ['/api/tuitions/{studentId}', '/api/tuitions/{studentId}/claim', '/api/tuitions/{studentId}/paid', '/api/tuitions/{studentId}/release']],
  payment: ['/v3/api-docs', ['/api/payments/request-otp', '/api/payments/verify', '/api/payment-transactions/history']],
  otp: ['/api-docs', ['/api/otp/generate', '/api/otp/verify']],
  email: ['/v3/api-docs', ['/api/emails/payment-success']],
};
const page = await fetch(`${origin}/docs`);
assert.equal(page.status, 200);
assert.match(await page.text(), /swagger-ui/);
console.log('PASS /docs HTTP 200');
await Promise.all(Object.entries(endpoints).map(async ([service, [specPath, paths]]) => {
  const base = `/docs-api/${service}`;
  const response = await fetch(origin + base + specPath, {signal: AbortSignal.timeout(30000)});
  assert.equal(response.status, 200, `${service} spec status`);
  const spec = await response.json();
  assert.match(spec.openapi, /^3\./);
  assert.equal(spec.servers[0].url, base);
  for (const path of paths) assert(spec.paths[path], `${service}: missing ${path}`);
  const serialized = JSON.stringify(spec);
  assert(!/ps-internal-service-token-|eyJ[A-Za-z0-9_-]+\./.test(serialized), `${service}: credential literal`);
  // Real non-mutating requests confirm proxy routing and preserved auth boundaries.
  const probe = service === 'auth' ? '/'
    : service === 'user' ? '/api/users/me'
    : service === 'balance' ? '/api/balances/me'
    : service === 'tuition' ? '/api/tuitions/100001'
    : service === 'payment' ? '/api/payment-transactions/history'
    : service === 'otp' ? '/api/otp/verify' : '/api/emails/payment-success';
  const options = {method: ['otp','email'].includes(service) ? 'POST' : 'GET', signal: AbortSignal.timeout(30000)};
  if (service === 'email') {
    options.headers = {'Content-Type':'application/json', 'X-Internal-Service-Token':'documentation-probe-invalid'};
    options.body = JSON.stringify({paymentId:'docs-probe',userId:'docs-probe',recipientEmail:'demo@example.com',fullName:'Demo',studentName:'Demo',studentId:'100001',amount:1,paidAt:'2026-11-03T07:00:00'});
  }
  const result = await fetch(origin + base + probe, options);
  assert.equal(result.status, service === 'auth' ? 200 : 401, `${service} proxy/security probe`);
  console.log(`PASS ${service}: ${paths.length} paths; proxy server ${base}; unauthenticated probe ${result.status}`);
}));
