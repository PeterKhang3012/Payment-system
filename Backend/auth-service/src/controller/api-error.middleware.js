// API error handling: normalize JSON parser errors and hide unexpected exception details.
export const apiErrorHandler = (error, req, res, next) => {
  if (res.headersSent) return next(error);
  if (error.code === 11000 && error.keyPattern?.username) {
    return res.status(409).json({ message: "Username already exists" });
  }
  const status = error.type === "entity.parse.failed" ? 400
    : error.type === "entity.too.large" ? 413 : 500;
  return res.status(status).json({ message: status === 400 ? "Invalid request body"
    : status === 413 ? "Request body is too large" : "Internal server error" });
};
