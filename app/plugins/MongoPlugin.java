package plugins;

import java.net.UnknownHostException;

import play.Application;
import play.Logger;
import play.Plugin;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

public class MongoPlugin extends Plugin {

	public static final String MONGOLAB_URI = "mongolab.uri";

	private final Application application;

	public static DB db;

	public MongoPlugin(Application application) {
		this.application = application;
	}

	@Override
	public void onStart() {

		try {
			String mongolabUri = application.configuration().getString(
					MONGOLAB_URI);

			Logger.info("Initializing MongoDB");

			MongoURI uri = new MongoURI(mongolabUri);
			Mongo mongo = new Mongo(uri);
			db = mongo.getDB(uri.getDatabase());
			db.authenticate(uri.getUsername(), uri.getPassword());

			Logger.info("Initialized MongoDB");

		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean enabled() {
		return (application.configuration().keys().contains(MONGOLAB_URI));
	}

}
