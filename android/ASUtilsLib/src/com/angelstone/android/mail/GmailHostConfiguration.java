package com.angelstone.android.mail;

import java.util.Properties;

public class GmailHostConfiguration implements MailHostConfiguration {
	private static final String SMTP_HOST = "smtp.gmail.com";

	@Override
	public Properties getMailHostProperties() {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", SMTP_HOST);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");
		
		return props;
	}

}
