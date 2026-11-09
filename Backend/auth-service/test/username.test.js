import test from 'node:test';
import assert from 'node:assert/strict';
import Auth from '../models/auth.model.js';

test('username normalization trims and preserves case', async () => {
  const user = new Auth({userId:'68d500000000000000000001', username:' Student01 ', password:'unused'});
  await user.validate();
  assert.equal(user.username,'Student01');
  assert.ok(Auth.schema.indexes().some(([keys, options]) => keys.username === 1 && options.unique));
});
