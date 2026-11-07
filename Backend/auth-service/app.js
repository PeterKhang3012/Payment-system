import dotenv from "dotenv" ;
import express from "express";
import http from "http";
import { fileURLToPath } from "node:url";
import connectMongo from "./config/db.js";
import cors from "cors";

//routes
import { apiErrorHandler } from "./src/controller/api-error.middleware.js";
import authRouter from "./src/routes/auth.route.js";

console.log("starting server...");

dotenv.config({ path: "./.env" });

const app = express();
app.use(cors({ origin: "http://localhost:5173" }));
app.use(express.json());

const server = http.createServer(app);


// Read-only documentation, independent of authentication business routes.
app.get("/openapi.json", (req, res) => {
    res.sendFile(fileURLToPath(new URL("./openapi.json", import.meta.url)));
});
app.use("/", authRouter);

app.get("/", (req, res) => {
    res.send("server is running");
});
// API error handling: handle parser and unexpected errors after routes.
app.use(apiErrorHandler);
const startServer = async () => {
    await connectMongo();
    server.listen(3000, () => {
        console.log(`Server is running on port 3000`);
    });
}
startServer();