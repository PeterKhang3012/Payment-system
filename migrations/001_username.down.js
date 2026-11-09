// Explicit rollback only. Existing userId_1 and _id_ indexes are preserved.
db.getSiblingDB('payment_system').auths.dropIndex('username_1');
