package plugins;

import io.iron.ironmq.Client;
import io.iron.ironmq.Queue;
import play.Application;
import play.Logger;
import play.Plugin;
import controllers.FindbugsThread;

public class IronMQPlugin extends Plugin {

	public static final String IRON_MQ_PROJECT_ID = "iron.mq.project.id";
	public static final String IRON_MQ_TOKEN = "iron.mq.token";

	private final Application application;

	public static Client client;

	public IronMQPlugin(Application application) {
		this.application = application;
	}

	@Override
	public void onStart() {
		Logger.info("Initializing Iron MQ Plugin");

		client = new Client(application.configuration().getString(
				IRON_MQ_PROJECT_ID), application.configuration().getString(
				IRON_MQ_TOKEN));

		Logger.info("Initialized Iron MQ Plugin");
	}

	@Override
	public boolean enabled() {
		return (application.configuration().keys().contains(IRON_MQ_PROJECT_ID));
	}
	
	public static final String FINDBUGS_QUEUE_NAME = "findbugs.queue";
	public static Queue getFindbugsQueue() {
		return client.queue(FINDBUGS_QUEUE_NAME);
	}
}
