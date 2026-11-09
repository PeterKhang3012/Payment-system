// Run in mongosh authenticated against admin, with application writers stopped.
const auths = db.getSiblingDB('payment_system').auths;
const duplicates = auths.aggregate([
  {$group: {_id: {$trim: {input: '$username'}}, ids: {$push: '$_id'}, count: {$sum: 1}}},
  {$match: {count: {$gt: 1}}}
], {collation: {locale: 'simple'}}).toArray();
if (duplicates.length) {
  printjson(duplicates);
  throw new Error('Duplicate username: migration stopped; no data changed');
}
const untrimmed = auths.find({$expr: {$ne: ['$username', {$trim: {input: '$username'}}]}}, {username: 1}).toArray();
if (untrimmed.length) {
  printjson(untrimmed);
  throw new Error('Untrimmed username: manual review required');
}
auths.createIndex({username: 1}, {unique: true, name: 'username_1', collation: {locale: 'simple'}});
printjson(auths.getIndexes());
