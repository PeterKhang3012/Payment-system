import fs from 'node:fs';
import assert from 'node:assert/strict';

const collection = JSON.parse(fs.readFileSync(new URL('../KDNPay API Documentation.postman_collection.json',import.meta.url),'utf8'));
function credentials(items) {
  for (const item of items ?? []) {
    if (item.item) { const found = credentials(item.item); if (found) return found; }
    try {
      const body = JSON.parse(item.request?.body?.raw);
      if (typeof body.username === 'string' && typeof body.password === 'string' && !body.password.includes('{{')) return body;
    } catch {}
  }
}
const account = credentials(collection.item);
assert.ok(account,'No seeded login fixture in the collection');
let checks = 0;
async function expect(url, options, status) {
  const response = await fetch(url, options);
  assert.equal(response.status,status,`Unexpected status for ${new URL(url).pathname}`);
  checks++;
  return response;
}
const json = body => ({method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body)});
const response = await expect('http://localhost:3000/login',json({...account,username:` ${account.username} `}),200);
const login = await response.json();
assert.equal(login.user.username,account.username); checks++;
await expect('http://localhost:3000/login',json({...account,username:account.username.toUpperCase()}),401);
await expect('http://localhost:3000/login',json({...account,password:'incorrect-unique-test'}),401);
const headers = {Authorization:`Bearer ${login.token}`};
await expect('http://localhost:3006/api/users/me',{headers},200);
await expect('http://localhost:3006/api/users/me',{},401);
const tuition = await (await expect('http://localhost:3002/api/tuitions/%20100001%20',{headers},200)).json();
assert.equal(tuition.studentId,'100001'); checks++;
for (const id of ['0','000','abc','123456789012345678901']) {
  await expect(`http://localhost:3002/api/tuitions/${id}`,{headers},400);
}
for (const id of [100001,' ','000','abc','123456789012345678901']) {
  const options = json({studentId:id,acceptedTerms:true}); options.headers = {...options.headers,...headers};
  await expect('http://localhost:8080/api/payments/request-otp',options,400);
}
// A missing tuition and an already-paid tuition fail before attempts/OTP generation.
for (const [studentId,status] of [['99999999999999999999',404],['100004',409]]) {
  const options=json({studentId,acceptedTerms:true}); options.headers={...options.headers,...headers};
  await expect('http://localhost:8080/api/payments/request-otp',options,status);
}
for (const [port,schema] of [[3002,'TuitionResponse'],[8080,'PaymentRequest']]) {
  const doc=await (await expect(`http://localhost:${port}/v3/api-docs`,{},200)).json();
  assert.equal(doc.components.schemas[schema].properties.studentId.type,'string'); checks++;
}
await expect('http://localhost:5173',{},200);
console.log(`PASS: ${checks} live API assertions; no OTP generated and no balance deduction requested`);
