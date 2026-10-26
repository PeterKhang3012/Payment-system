// expiresAt is a backend LocalDateTime without an offset, not a browser instant.
// This timer is advisory; only backend verification determines actual expiry.
export function otpDeadline(response: { paymentId: string; expiresInSeconds: number }, requestedAt: number): number {
    if (!response || typeof response.paymentId !== 'string' || !response.paymentId.trim()
        || !Number.isFinite(response.expiresInSeconds) || response.expiresInSeconds <= 0) {
        throw new Error('INVALID_OTP_RESPONSE');
    }
    return requestedAt + response.expiresInSeconds * 1000;
}
export function otpSeconds(deadline: number, now: number): number {
    return Math.max(0, Math.ceil((deadline - now) / 1000));
}
