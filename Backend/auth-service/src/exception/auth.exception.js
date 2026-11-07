// API error handling: distinguish invalid credentials from unexpected authentication failures.
export class InvalidCredentialsError extends Error {
  constructor() { super("INVALID_CREDENTIALS"); }
}
