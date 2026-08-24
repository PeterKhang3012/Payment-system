import mongoose from "mongoose";

const connectMongo = async() => {
    try{
        await mongoose.connect(process.env.MONGO_URL);
        console.log("Mongo connected");
    }catch (error){
        console.error("MongoDB connection failed:", error.message);
        process.exit(1);
    };
}

export default connectMongo;