import test from "node:test";
import assert from "node:assert/strict";
import { apiErrorHandler } from "../src/controller/api-error.middleware.js";
// API error regression: parser errors differ from internal failures and never expose details.
for (const [type, status] of [["entity.parse.failed",400],["entity.too.large",413],[undefined,500]]) {
 test('safe error response ' + status, () => {
  let result;
  const res = { status(code) { assert.equal(code,status); return this; }, json(body) { result=body; } };
  apiErrorHandler({type,message:'SQL password private-key'}, {}, res, () => assert.fail());
  assert.ok(result.message); assert.ok(!JSON.stringify(result).includes('private-key'));
 });
}

test('duplicate username returns a safe conflict', () => {
 let result;
 const res = { status(code) { assert.equal(code,409); return this; }, json(body) { result=body; } };
 apiErrorHandler({code:11000,keyPattern:{username:1},message:'database private details'}, {}, res, () => assert.fail());
 assert.deepEqual(result,{message:'Username already exists'});
});
