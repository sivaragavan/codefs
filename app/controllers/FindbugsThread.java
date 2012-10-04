package controllers;

import io.iron.ironmq.EmptyQueueException;
import io.iron.ironmq.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.Artifact;
import models.Project;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import play.Logger;
import plugins.FindbugsPlugin;
import plugins.IronMQPlugin;
import plugins.MongoPlugin;
import plugins.S3Plugin;
import utils.CodeFSConstants;
import utils.Mailer;
import views.html.emails.artifactfbcompleted;

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
			} catch (EmptyQueueException e1) {
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (m != null) {
				String jsonString = m.getBody();
				String artifactId = null;
				String projectId = null;
				try {
					JSONObject json = new JSONObject(jsonString);
					artifactId = json.getString("artifactId");
					projectId = json.getString("projectId");
				} catch (JSONException e3) {
					e3.printStackTrace();
				}

				System.out.println("Analyzing : " + artifactId);

				System.out.println(projectId);

				Project project = MongoPlugin.ds.find(Project.class)
						.field("_id").equal(new ObjectId(projectId)).get();

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

				System.out.println("Analyzing (Running find bugs - Run 1) : "
						+ a.id);

				try {

					String commandName = "findbugs.bat";

					String os = System.getProperty("os.name").toLowerCase();

					System.out.println(os);

					if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0
							|| os.indexOf("mac") >= 0) {
						commandName = "findbugs";
					}

					Process p = Runtime.getRuntime().exec(
							"./findbugs-2.0.1/exec/" + commandName
									+ " -textui -xml " + " -output "
									+ a.id.toString() + "-" + a.artifactName
									+ ".xml " + a.id.toString() + "-"
									+ a.artifactName);
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

				System.out.println("Analyzing (Finding bug counts) : " + a.id);

				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document dom = db.parse(a.id.toString() + "-"
							+ a.artifactName + ".xml");

					Element docEle = dom.getDocumentElement();

					NodeList nl = docEle
							.getElementsByTagName("FindBugsSummary");
					if (nl != null && nl.getLength() > 0) {
						Element el = (Element) nl.item(0);
						String code_size = el.getAttribute("total_size");
						if (code_size.equals(""))
							code_size = "0";
						String total = el.getAttribute("total_bugs");
						if (total.equals(""))
							total = "0";
						String priority_1 = el.getAttribute("priority_1");
						if (priority_1.equals(""))
							priority_1 = "0";
						String priority_2 = el.getAttribute("priority_2");
						if (priority_2.equals(""))
							priority_2 = "0";
						String priority_3 = el.getAttribute("priority_3");
						if (priority_3.equals(""))
							priority_3 = "0";

						System.out.println("Code Size : " + code_size);
						System.out.println("Total bugs : " + total);
						System.out.println("Priority 1 bugs : " + priority_1);
						System.out.println("Priority 2 bugs : " + priority_2);
						System.out.println("Priority 3 bugs : " + priority_3);

						a.report.code_size = Integer.parseInt(code_size);
						a.report.total = Integer.parseInt(total);
						a.report.priority_1 = Integer.parseInt(priority_1);
						a.report.priority_2 = Integer.parseInt(priority_2);
						a.report.priority_3 = Integer.parseInt(priority_3);

						MongoPlugin.ds.save(a);
					}

				} catch (ParserConfigurationException pce) {
					pce.printStackTrace();
				} catch (SAXException se) {
					se.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}

				System.out.println("Analyzing (Running find bugs - Run 2) : "
						+ a.id);

				try {

					String commandName = "findbugs.bat";

					String os = System.getProperty("os.name").toLowerCase();

					System.out.println(os);

					if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0
							|| os.indexOf("mac") >= 0) {
						commandName = "findbugs";
					}

					Process p = Runtime.getRuntime().exec(
							"./findbugs-2.0.1/exec/" + commandName
									+ " -textui -html:fancy-hist.xsl "
									+ " -output " + a.id.toString() + "-"
									+ a.artifactName + ".html "
									+ a.id.toString() + "-" + a.artifactName);
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
							+ S3Plugin.s3Bucket + "/" + a.id.toString() + "/"
							+ a.artifactName + ".html";
					MongoPlugin.ds.save(a);
				}

				FindbugsPlugin.delete(m);

				System.out.println(project.users);

				Mailer.sendMail(CodeFSConstants.artifact_findbugs_completed,
						artifactfbcompleted.render(project, a).toString(),
						"Project Created", project.users);

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
