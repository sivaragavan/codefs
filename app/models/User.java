package models;

import org.bson.types.ObjectId;

import plugins.MongoPlugin;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class User {

	static DBCollection coll = MongoPlugin.db.getCollection("users");

	public static DBObject getUserByEmail(String email) {
		BasicDBObject doc = new BasicDBObject();
		doc.put("email", email);
		return coll.findOne(doc);
	}

	public static DBObject getUserById(String id) {
		BasicDBObject doc = new BasicDBObject();
		doc.put("_id", new ObjectId(id));
		return coll.findOne(doc);
	}

	public static DBObject addUser(String email) {
		BasicDBObject doc = new BasicDBObject();
		doc.put("email", email);
		coll.insert(doc, WriteConcern.SAFE);
		return doc;
	}

	public static DBObject addFollowee(String followerId, String followeeId) {
		BasicDBObject follower = new BasicDBObject();
		follower.put("_id", new ObjectId(followerId));

		BasicDBObject newFollower = new BasicDBObject();
		newFollower.put("$push",
				new BasicDBObject().append("follows", followeeId));

		coll.update(follower, newFollower);

		return newFollower;
	}

	public static BasicDBList listAll() {

		BasicDBList list = new BasicDBList();
		int counter = 0;

		DBCursor cursor = coll.find();

		while (cursor.hasNext()) {
			DBObject user = cursor.next();
			list.put(
					counter,
					new BasicDBObject().append("userId",
							user.get("_id").toString()).append("email",
							user.get("email")));
			counter++;
		}

		return list;

	}

}
