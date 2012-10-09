package models;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("users")
public class User {

	@Id
	public ObjectId id;
	public String emailId;

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
