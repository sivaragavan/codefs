package plugins;

import io.iron.ironmq.Client;
import io.iron.ironmq.Message;
import io.iron.ironmq.Queue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import models.Artifact;

import org.bson.types.ObjectId;

import play.Application;
import play.Logger;
import play.Plugin;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import controllers.FindbugsThread;

public class FindbugsPlugin extends Plugin {

	public static final String FINDBUGS_ENABLE = "findbugs.enable";
	public static final String FINDBUGS_QUEUE_NAME = "findbugs.queue";
	public static final String IRON_MQ_PROJECT_ID = "iron.mq.project.id";
	public static final String IRON_MQ_TOKEN = "iron.mq.token";

	private final Application application;

	public static Queue queue;

	public FindbugsPlugin(Application application) {
		this.application = application;
	}

	@Override
	public void onStart() {
		Logger.info("Initializing Findbugs Plugin");

		Client client = new Client(application.configuration().getString(
				IRON_MQ_PROJECT_ID), application.configuration().getString(
				IRON_MQ_TOKEN));
		queue = client.queue(FINDBUGS_QUEUE_NAME);

		new Thread(new FindbugsThread()).start();
	}

	@Override
	public boolean enabled() {
		return (application.configuration().getBoolean(FINDBUGS_ENABLE));
	}

	public static void put(Artifact a) {
		try {
			queue.push(a.id.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
