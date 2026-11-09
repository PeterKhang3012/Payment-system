import mongoose from 'mongoose';

const authSchema = new mongoose.Schema({
    userId: {
    type: mongoose.Schema.Types.ObjectId,
    required: true,
    unique: true,
  },

  username: {
    type: String,
    required: true,
    trim: true,
    unique: true,
  },

  password:{
    type: String,
    required: true,
  }
});

export default mongoose.model("Auth", authSchema);
