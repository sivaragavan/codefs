package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import models.Artifact;
import models.Project;
import models.User;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import plugins.FindbugsPlugin;
import plugins.MongoPlugin;
import plugins.S3Plugin;
import views.html.upload;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class Application extends Controller {

	public static Result index() throws Exception {
		return ok(views.html.index.render());
	}

	public static Result dashboard(String projectId) throws Exception {
		Project project = MongoPlugin.ds.find(Project.class).field("_id")
				.equal(new ObjectId(projectId)).get();

		return ok(views.html.dashboard.render(project));
	}

	public static Result create(String projectName, String email)
			throws Exception {
		Project project = createProject(projectName, email);
		return ok(project.toString());
	}

	public static Result userList(String projectId) throws Exception {
		Project project = MongoPlugin.ds.find(Project.class).field("_id")
				.equal(new ObjectId(projectId)).get();

		JSONObject response = new JSONObject();
		response.put("users", new JSONArray(project.users.toString()));

		return ok(response.toString());
	}

	public static Result userAdd(String projectId, String emailId,
			String message) throws Exception {

		User u = MongoPlugin.ds.find(User.class).field("emailId")
				.equal(emailId).get();
		if (u == null) {
			u = new User(emailId);
			MongoPlugin.ds.save(u);
		}

		Project project = MongoPlugin.ds.find(Project.class).field("_id")
				.equal(new ObjectId(projectId)).get();

		project.users.add(u);
		MongoPlugin.ds.save(project);

		JSONObject response = new JSONObject();
		response.put("users", new JSONArray(project.users.toString()));

		return ok(response.toString());
	}

	public static Result artifactList(String projectId) throws Exception {
		Project project = MongoPlugin.ds.find(Project.class).field("_id")
				.equal(new ObjectId(projectId)).get();

		JSONObject response = new JSONObject();
		response.put("artifacts", new JSONArray(project.artifacts.toString()));

		return ok(response.toString());
	}

	public static Result artifactAdd(String projectId) throws Exception {

		MultipartFormData body = request().body().asMultipartFormData();
		FilePart filePart = body.getFile("file");
		String fileName = filePart.getFilename();
		File fileFromWeb = filePart.getFile();

		System.out.println("Received File : " + fileName);

		Artifact a = new Artifact(fileName);
		MongoPlugin.ds.save(a);

		Project project = MongoPlugin.ds.find(Project.class).field("_id")
				.equal(new ObjectId(projectId)).get();

		project.artifacts.add(a);
		MongoPlugin.ds.save(project);

		System.out.println("Uploading to S3 : " + a.id);

		if (S3Plugin.amazonS3 == null) {
			Logger.error("Could not save because amazonS3 was null");
			throw new RuntimeException("Could not save");
		} else {
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					S3Plugin.s3Bucket, a.id.toString() + "/" + a.artifactName,
					fileFromWeb);
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
			S3Plugin.amazonS3.putObject(putObjectRequest);
		}

		System.out.println("FindBugs analyze start : " + a.id);

		FindbugsPlugin.put(a);

		System.out.println("Responding to Add Artifact : " + a.id);

		return ok(upload.render());
	}

	public static Project createProject(String projectName, String email) {
		User u = MongoPlugin.ds.find(User.class).field("emailId").equal(email)
				.get();
		if (u == null) {
			u = new User(email);
			MongoPlugin.ds.save(u);
		}

		Project project = new Project(projectName, u);
		MongoPlugin.ds.save(project);

		return project;
	}
}