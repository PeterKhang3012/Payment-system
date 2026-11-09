import fs from 'node:fs';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {randomUUID} from 'node:crypto';
import Auth from '../Backend/auth-service/models/auth.model.js';
const require=createRequire(new URL('../Backend/auth-service/package.json',import.meta.url));
const mongoose=require('mongoose');
const env=Object.fromEntries(fs.readFileSync(new URL('../.env',import.meta.url),'utf8').split(/\r?\n/)
  .filter(line=>/^\s*[A-Za-z_][A-Za-z0-9_]*=/.test(line))
  .map(line=>{const at=line.indexOf('=');return [line.slice(0,at).trim(),line.slice(at+1).trim().replace(/^(['"])(.*)\1$/,'$2')];}));
await mongoose.connect('mongodb://localhost:27017/payment_system',{
  user:env.MONGO_ROOT_USERNAME,pass:env.MONGO_ROOT_PASSWORD,authSource:'admin'
});
const name=`__unique_probe_${randomUUID().replaceAll('-','')}`;
const Probe=mongoose.model(`UniqueProbe${Date.now()}`,Auth.schema.clone(),name);
let created=false;
try {
  const indexes=await mongoose.connection.db.collection('auths').indexes();
  assert.ok(indexes.some(i=>i.name==='username_1'&&i.unique));
  assert.ok(indexes.some(i=>i.name==='userId_1'&&i.unique));
  await Probe.createCollection(); created=true;
  await Probe.init();
  const insert=username=>Probe.create({userId:new mongoose.Types.ObjectId(),username,password:'test-fixture-only'});
  const results=await Promise.allSettled([insert(' RaceUser '),insert('RaceUser')]);
  assert.equal(results.filter(r=>r.status==='fulfilled').length,1);
  assert.equal(results.find(r=>r.status==='rejected').reason.code,11000);
  assert.equal((await Probe.findOne()).username,'RaceUser');
  await insert('raceuser');
  assert.equal(await Probe.countDocuments(),2);
  console.log('PASS: username trim, new insert, duplicate rejection, case-sensitive uniqueness and concurrent race');
} finally {
  // Delete only the disposable collection created by this verification, never auths or user data.
  if (created) await mongoose.connection.db.dropCollection(name);
  await mongoose.disconnect();
}
