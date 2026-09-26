import Auth from "../../models/auth.model.js";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import fs from "fs";
const secret = fs.readFileSync("./keys/private.pem", "utf8");
//login
const login = async (username, password) => {
  const user = await Auth.findOne({ username });

  if (!user) {
    throw new Error("INVALID_CREDENTIALS");
  }

  const isMatch = await bcrypt.compare(password, user.password);
  if (!isMatch) {
    throw new Error("INVALID_CREDENTIALS");
  }

  const token = jwt.sign(
    { userId: user.userId, username: user.username },
    secret,
    { algorithm: "RS256", 
    expiresIn: process.env.JWT_EXPIRES_IN || "1h" },
  );

  return {
    token,
    user: {
      userId: user.userId,
      username: user.username
    },
  };
};

export {login};