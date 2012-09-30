package controllers;

import io.iron.ironmq.Message;

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

import play.Logger;
import plugins.FindbugsPlugin;
import plugins.IronMQPlugin;
import plugins.MongoPlugin;
import plugins.S3Plugin;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class FindbugsThread implements Runnable {

	public void run() {

		while (true) {

			Logger.info("Waiting for request : Findbugs");

			Message m = null;
			try {
				m = IronMQPlugin.getFindbugsQueue().get();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (m != null) {
				String artifactId = m.getBody();

				System.out.println("Analyzing : " + artifactId);

				Artifact a = MongoPlugin.ds.find(Artifact.class).field("_id")
						.equal(new ObjectId(artifactId)).get();

				System.out.println("Analyzing (Saving file to disk) : "
						+ a.artifactName);

				try {

					GetObjectRequest rangeObjectRequest = new GetObjectRequest(
							S3Plugin.s3Bucket, a.id.toString() + "/"
									+ a.artifactName);
					S3Object s3Object = S3Plugin.amazonS3
							.getObject(rangeObjectRequest);

					InputStream in = s3Object.getObjectContent();

					File f2 = new File(a.id.toString() + "-" + a.artifactName);
					OutputStream out = new FileOutputStream(f2);

					byte[] buf = new byte[1024];
					int len;

					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}

					System.out.println(f2.getAbsolutePath());

					in.close();
					out.close();
				} catch (FileNotFoundException ex) {
					System.out.println(ex.getMessage()
							+ " in the specified directory.");
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}

				System.out.println("Analyzing (Running find bugs) : " + a.id);

				try {

					String commandName = "findbugs.bat";

					String os = System.getProperty("os.name").toLowerCase();
					if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
						commandName = "findbugs";
					}

					Process p = Runtime.getRuntime().exec(
							"./findbugs-2.0.1/exec/" + commandName
									+ " -textui -html:plain.xsl "
									+ a.id.toString() + "-" + a.artifactName
									+ " > " + a.id.toString() + "-"
									+ a.artifactName + ".html");
					p.waitFor();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(p.getInputStream()));
					String line = reader.readLine();
					while (line != null) {
						System.out.println(line);
						line = reader.readLine();
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}

				System.out.println("Analyzing (uploading result to s3) : "
						+ a.id);

				if (S3Plugin.amazonS3 == null) {
					Logger.error("Could not save because amazonS3 was null");
					throw new RuntimeException("Could not save");
				} else {
					File f3 = new File(a.id.toString() + "-" + a.artifactName
							+ ".html");
					PutObjectRequest putObjectRequest = new PutObjectRequest(
							S3Plugin.s3Bucket, a.id.toString() + "/"
									+ a.artifactName + ".html", f3);
					putObjectRequest
							.withCannedAcl(CannedAccessControlList.PublicRead);
					S3Plugin.amazonS3.putObject(putObjectRequest);

					a.findBugsReport = "https://s3.amazonaws.com/"
							+ S3Plugin.s3Bucket + "/" + a.id.toString() + "-"
							+ a.artifactName + ".html";
					MongoPlugin.ds.save(a);
				}

				System.out.println("Analyzing Complete : " + a.id);

			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
