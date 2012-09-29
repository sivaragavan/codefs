package models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

@Entity("projects")
public class Project {

	@Id
	public ObjectId id;
	public String projectName;
	@Reference
	public List<User> users = new ArrayList<User>();
	@Reference
	public List<Artifact> artifacts = new ArrayList<Artifact>();
	public long timestamp;

	public Project() {
	}

	public Project(String projectName, User user) {
		this.projectName = projectName;
		this.users.add(user);
		this.timestamp = System.currentTimeMillis();
	}

	public String toString() {
		JSONObject response = new JSONObject();
		try {
			response.put("id", id);
			response.put("projectName", projectName);
			response.put("timestamp", timestamp);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response.toString();
	}

}
