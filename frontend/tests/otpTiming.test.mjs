import test from 'node:test';
import process from 'node:process';
import assert from 'node:assert/strict';
import { otpDeadline, otpSeconds } from '../src/otpTiming.ts';

test('new OTP keeps its lifetime regardless of timezone-less expiresAt', () => {
    const original = process.env.TZ;
    try {
        for (const zone of ['UTC', 'Asia/Bangkok', 'America/New_York']) {
            process.env.TZ = zone;
            const response = {paymentId:'test-payment', expiresInSeconds:300, expiresAt:'2026-10-24T01:05:00'};
            const deadline = otpDeadline(response, 1000);
            assert.equal(otpSeconds(deadline, 2000), 299);
            assert.equal(otpSeconds(deadline, 301000), 0);
        }
    } finally { if (original === undefined) delete process.env.TZ; else process.env.TZ = original; }
});
test('accounts for request delay and never shows negative time', () => {
    const deadline = otpDeadline({paymentId:'test-payment',expiresInSeconds:300},1000);
    assert.equal(otpSeconds(deadline,11000),290);
    assert.equal(otpSeconds(deadline,500000),0);
});
test('missing or malformed OTP responses cannot activate the OTP form', () => {
    for (const value of [null, {}, {paymentId:'',expiresInSeconds:300}, {paymentId:'x',expiresInSeconds:NaN}, {paymentId:'x',expiresInSeconds:0}]) {
        assert.throws(()=>otpDeadline(value,0),/INVALID_OTP_RESPONSE/);
    }
});
test('a new valid OTP request receives its own deadline', () => {
    assert.equal(otpDeadline({paymentId:'new-payment',expiresInSeconds:300},600000),900000);
});
