package controllers;

import models.Project;
import models.User;

import org.bson.types.ObjectId;

import play.mvc.Controller;
import play.mvc.Result;
import plugins.MongoPlugin;

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