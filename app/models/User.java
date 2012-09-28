package models;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

@Entity("users")
public class User {

	@Id
	private ObjectId id;
	private String emailId;

	public User() {

	}

	public User(String emailId) {
		this.emailId = emailId;
	}

	public String toString() {
		JSONObject response = new JSONObject();
		try {
			response.put("id", id);
			response.put("emailId", emailId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response.toString();
	}

}
