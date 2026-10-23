import express from "express";
const router = express.Router();
import { loginController } from "../controller/auth.controller.js";

// login
router.post("/login", loginController);

export default router;