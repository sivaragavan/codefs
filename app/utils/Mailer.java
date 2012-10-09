package utils;

import java.util.ArrayList;
import java.util.List;

import models.User;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

public class Mailer {
	public static void sendMail(String subject, String html, String text,
			List<User> users) {
		MailerAPI mail = play.Play.application().plugin(MailerPlugin.class)
				.email();
		mail.setSubject(subject);
		java.util.Iterator<User> it = users.iterator();

		List<String> userIds = new ArrayList<String>();

		while (it.hasNext()) {
			userIds.add(it.next().emailId);
		}

		String[] userArray = userIds.toArray(new String[0]);

		mail.addRecipient(userArray);
		mail.addFrom("CodeFS Notification <codefs.noreply@gmail.com>");
		mail.send(text, html);
	}
	
	public static void sendMail(String subject, String html, String text,
			User user) {
		MailerAPI mail = play.Play.application().plugin(MailerPlugin.class)
				.email();
		mail.setSubject(subject);
		
		mail.addRecipient(user.emailId);
		mail.addFrom("CodeFS Notification <codefs.noreply@gmail.com>");
		mail.send(text, html);
	}
}
