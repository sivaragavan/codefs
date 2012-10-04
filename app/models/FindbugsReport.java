package models;

import org.json.JSONException;
import org.json.JSONObject;

public class FindbugsReport {
	public int code_size;
	public int total;
	public int priority_1;
	public int priority_2;
	public int priority_3;

	public String toString() {
		JSONObject response = new JSONObject();
		try {
			response.put("code_size", code_size);
			response.put("total", total);
			response.put("priority_1", priority_1);
			response.put("priority_2", priority_2);
			response.put("priority_3", priority_3);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response.toString();
	}

}
