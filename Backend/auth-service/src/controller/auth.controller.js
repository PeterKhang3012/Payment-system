import { InvalidCredentialsError } from "../exception/auth.exception.js";
import { login } from "../services/auth.service.js";

const loginController = async (req, res) => {
  try {
    // API validation: reject missing or incorrectly typed credentials before authentication.
    const { username, password } = req.body ?? {};

    if (typeof username !== "string" || username.trim().length === 0) {
      return res.status(400).json({
        message: "Username is required and must be a string",
      });
    }

    if (typeof password !== "string" || password.length === 0) {
      return res.status(400).json({
        message: "Password is required and must be a string",
      });
    }

    const data = await login(username, password);

    console.log("Login successful");
    return res.status(200).json(data);
  } catch (error) {
    // API error handling: only a known credential rejection is 401.
    if (error instanceof InvalidCredentialsError) {
      return res.status(401).json({
        message: "Username or password is incorrect",
      });
    }
    // Security: avoid logging credentials or uncontrolled infrastructure error details.
    console.error("Login failed due to an internal error");

    return res.status(500).json({
      message: "Internal server error",
    });
  }
};

export { loginController };