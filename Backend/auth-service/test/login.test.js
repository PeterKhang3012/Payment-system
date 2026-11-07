import test from "node:test";
import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import { InvalidCredentialsError } from "../src/exception/auth.exception.js";

// API regression: mock only authentication so validation cannot query storage or load private keys.
registerHooks({ load(url, context, nextLoad) {
  if (url.endsWith('/src/services/auth.service.js')) {
    return { format: 'module', shortCircuit: true,
      source: 'export const login = (...args) => globalThis.__loginMock(...args);' };
  }
  return nextLoad(url, context);
} });
const { loginController } = await import('../src/controller/auth.controller.js');
async function run(body, behavior) {
  let calls = 0, status, response;
  globalThis.__loginMock = async (...args) => { calls++; return behavior(...args); };
  await loginController({body}, {status(value) { status=value; return this; }, json(value) { response=value; }});
  return { calls, status, response };
}
test('invalid credentials shape never reaches authentication', async () => {
  for (const body of [undefined,null,{},[],{username:1,password:'x'}, {username:'',password:'x'},
    {username:'u',password:null},{username:'u',password:{}},{username:'u',password:''}]) {
    const result = await run(body, () => assert.fail());
    assert.equal(result.status,400); assert.equal(result.calls,0);
  }
});
test('valid request preserves success response', async () => {
  const data={token:'mock-token',user:{userId:'1',username:'u'}};
  const result=await run({username:'u',password:'p'}, (u,p) => {
    assert.equal(u,'u'); assert.equal(p,'p'); return data;
  });
  assert.equal(result.status,200); assert.deepEqual(result.response,data);
});
test('invalid credentials return 401',async () => {
  const result=await run({username:'u',password:'p'},()=>{throw new InvalidCredentialsError();});
  assert.equal(result.status,401);
});
test('unexpected authentication failure returns sanitized 500',async () => {
  for (const error of [new Error('SQL private-key password'),new Error('INVALID_CREDENTIALS'),null]) {
    const result=await run({username:'u',password:'p'},()=>{throw error;});
    assert.equal(result.status,500); assert.deepEqual(result.response,{message:'Internal server error'});
  }
});
