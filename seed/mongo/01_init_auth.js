db = db.getSiblingDB("payment_system");

db.auths.insertMany([
    {
        userId: ObjectId("68d500000000000000000001"),
        username: "student00",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe" //tất cả mk là Test12345
    },
    {
        userId: ObjectId("68d500000000000000000002"),
        username: "student01",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000003"),
        username: "student02",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000004"),
        username: "student03",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000005"),
        username: "student04",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000006"),
        username: "student05",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000007"),
        username: "student06",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000008"),
        username: "student07",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000009"),
        username: "student08",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    },
    {
        userId: ObjectId("68d500000000000000000010"),
        username: "student09",
        password: "$2a$12$A68eqrNJDkGe6aZps5MpWeBkjyyHGoSjuB9vPioLoGGgQrD.chSHe"
    }
]);

db.auths.createIndex({ username: 1 }, { unique: true, name: "username_1", collation: { locale: "simple" } });
