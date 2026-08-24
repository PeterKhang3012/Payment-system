import {generateKeyPairSync} from "crypto";
import fs from "fs";
import path from "path";

const keydir = path.join(process.cwd(), "keys");

if (!fs.existsSync(keydir)) {
  fs.mkdirSync(keydir, { recursive: true });
}

const { publicKey, privateKey } = generateKeyPairSync("rsa", {
  modulusLength: 2048,

  publicKeyEncoding: {
    type: "spki",
    format: "pem",
  },

  privateKeyEncoding: {
    type: "pkcs8",
    format: "pem",
  },
});

fs.writeFileSync(path.join(keydir, "private.pem"), privateKey);
fs.writeFileSync(path.join(keydir, "public.pem"), publicKey);

console.log("Keys generated successfully!");