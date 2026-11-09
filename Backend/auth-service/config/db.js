import mongoose from "mongoose";

const connectMongo = async() => {
    try{
        await mongoose.connect(            
            "mongodb://mongodb:27017/payment_system",
            {
                user: process.env.MONGO_ROOT_USERNAME,
                pass: process.env.MONGO_ROOT_PASSWORD,
                authSource: "admin",
            });
        console.log("Mongo connected");
    }catch (error){
        console.error("MongoDB connection failed:", error.message);
        process.exit(1);
    };
}

export default connectMongo;