// Notification Service - MongoDB collections and indexes

db = db.getSiblingDB("notification_db");

db.createCollection("notifications");
db.notifications.createIndex({ "eventId": 1 }, { unique: true });
db.notifications.createIndex({ "accountId": 1, "createdAt": -1 });
db.notifications.createIndex({ "channel": 1, "status": 1 });

db.createCollection("notification_log");
db.notification_log.createIndex({ "createdAt": 1 });
