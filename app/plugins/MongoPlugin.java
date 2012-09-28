package plugins;

import java.net.UnknownHostException;

import models.Project;

import play.Application;
import play.Logger;
import play.Plugin;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

public class MongoPlugin extends Plugin {

	public static final String MONGOLAB_URI = "mongolab.uri";

	private final Application application;

	public static Datastore ds;

	public MongoPlugin(Application application) {
		this.application = application;
	}

	@Override
	public void onStart() {

		try {
			Logger.info("Initializing MongoDB");

			Morphia morphia = new Morphia();
			morphia.map(Project.class);

			MongoURI uri = new MongoURI(application.configuration().getString(
					MONGOLAB_URI));
			Mongo mongo = new Mongo(uri);

			ds = morphia.createDatastore(mongo, uri.getDatabase(),
					uri.getUsername(), uri.getPassword());

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
