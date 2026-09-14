import dotenv from "dotenv" ;
import express from "express";
import http from "http";
import connectMongo from "./config/db.js";

//routes
import authRouter from "./src/routes/auth.route.js";

console.log("starting server...");

dotenv.config({ path: "./.env" });

const app = express();
app.use(express.json());

const server = http.createServer(app);


app.use("/", authRouter);

app.get("/", (req, res) => {
    res.send("server is running");
});
const startServer = async () => {
    await connectMongo();
    server.listen(process.env.PORT, () => {
        console.log(`Server is running on port ${process.env.PORT}`);
    });
}
startServer();