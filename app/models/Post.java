package models;

import plugins.MongoPlugin;
import scala.actors.threadpool.Arrays;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class Post {

	static DBCollection coll = MongoPlugin.db.getCollection("posts");

	public static DBObject addPost(String userId) {
		BasicDBObject doc = new BasicDBObject();
		doc.put("userId", userId);
		doc.put("timestamp", System.currentTimeMillis());
		coll.insert(doc, WriteConcern.SAFE);
		return doc;
	}

	public static BasicDBList feed(BasicDBList follows) {

		BasicDBList list = new BasicDBList();
		int counter = 0;

		BasicDBObject doc = new BasicDBObject();
		doc.put("userId", new BasicDBObject().append("$in", follows));
		DBCursor cursor = coll.find(doc);

		while (cursor.hasNext()) {
			DBObject user = cursor.next();
			list.put(counter, new BasicDBObject().append("postId",
					user.get("_id").toString()));
			counter++;
		}

		return list;

	}

}
