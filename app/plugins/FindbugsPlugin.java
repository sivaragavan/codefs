package plugins;

import io.iron.ironmq.Message;

import java.io.IOException;

import models.Artifact;
import models.Project;

import org.json.JSONObject;

import play.Application;
import play.Logger;
import play.Plugin;
import controllers.FindbugsThread;

public class FindbugsPlugin extends Plugin {

	public static final String FINDBUGS_ENABLE = "findbugs.enable";

	private final Application application;

	public FindbugsPlugin(Application application) {
		this.application = application;
	}

	public void onStart() {
		Logger.info("Initializing Findbugs Plugin");
		new Thread(new FindbugsThread()).start();
		Logger.info("Initialized Findbugs Plugin");
	}

	@Override
	public boolean enabled() {
		return (application.configuration().getBoolean(FINDBUGS_ENABLE));
	}

	public static void put(Artifact a, Project p) {
		try {
			JSONObject request = new JSONObject();
			request.put("projectId", p.id.toString());
			request.put("artifactId", a.id.toString());
			IronMQPlugin.getFindbugsQueue().push(request.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void delete(Message m) {
		try {
			IronMQPlugin.getFindbugsQueue().deleteMessage(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
