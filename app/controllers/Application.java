package controllers;

import models.Post;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class Application extends Controller {

	public static Result login(String email) throws Exception {

		DBObject user = User.getUserByEmail(email);
		if (user == null) {
			user = User.addUser(email);
		}

		return ok(user.toString());
	}

	public static Result post(String userId) throws Exception {

		DBObject user = User.getUserById(userId);
		if (user == null) {
			return notFound();
		}

		DBObject post = Post.addPost(userId);

		return ok(post.toString());
	}

	public static Result list() throws Exception {

		BasicDBList result = User.listAll();

		return ok(result.toString());
	}

	public static Result follow(String followerId, String followeeId)
			throws Exception {

		DBObject follower = User.getUserById(followerId);
		DBObject followee = User.getUserById(followeeId);

		if (followee == null || follower == null) {
			return notFound();
		}

		follower = User.addFollowee(followerId, followeeId);

		return ok(follower.toString());
	}

	public static Result feed(String userId, Integer page) throws Exception {

		DBObject follower = User.getUserById(userId);

		if (follower == null) {
			return notFound();
		}

		BasicDBList follows = (BasicDBList) follower.get("follows");

		BasicDBList result = Post.feed(follows);

		return ok(result.toString());
	}
}