package models;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("artifacts")
public class Artifact {

	@Id
	public ObjectId id;
	public String artifactName;
	public String findBugsReport;

	public Artifact() {
	}

	public Artifact(String artifactName) {
		this.artifactName = artifactName;
	}

	public String toString() {
		JSONObject response = new JSONObject();
		try {
			response.put("id", id);
			response.put("artifactName", artifactName);
			response.put("findBugsReport", findBugsReport);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response.toString();
	}

}
