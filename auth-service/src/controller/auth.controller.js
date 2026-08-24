import { login } from "../services/auth.service.js";

const loginController = async (req, res) => {
  try {
    const { username, password } = req.body;
    const data = await login(username, password);
    console.log("login successful");
    res.json(data);
  } catch (error) {
    console.log("Login Error:", error);
    if (error.message === "INVALID_CREDENTIALS") {
      return res
        .status(401)
        .json({ message: "Username or password is incorrect!" });
    }
    res.status(500).json({ message: error.message });
  }
};

export { loginController };