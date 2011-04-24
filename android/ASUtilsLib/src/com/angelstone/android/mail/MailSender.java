package com.angelstone.android.mail;

import java.util.Calendar;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailSender extends javax.mail.Authenticator {
	private String mUser;
	private String mPassword;
	private String mSubject;
	private String mSender;
	private String mRecipients;
	private String mBody;

	private MimeMultipart mMultiPart;
	private MailHostConfiguration mHostConfig;

	public MailSender(MailHostConfiguration config) {
		mHostConfig = config;

		MailcapCommandMap mc = (MailcapCommandMap) CommandMap
				.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);

		mMultiPart = new MimeMultipart();
	}

	public MailSender(MailHostConfiguration config, String user, String password) {
		this(config);

		this.mUser = user;
		this.mPassword = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(mUser, mPassword);
	}

	public void addAttachment(String filename) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);

		mMultiPart.addBodyPart(messageBodyPart);
	}

	public void sendMail() throws AddressException, MessagingException {
		Session session = Session.getDefaultInstance(
				mHostConfig.getMailHostProperties(), this);

		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress(mSender));
		message.setSender(new InternetAddress(mSender));
		message.setSubject(mSubject);
		message.setSentDate(Calendar.getInstance().getTime());

		if (mRecipients.indexOf(',') > 0)
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(mRecipients));
		else
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(
					mRecipients));

		BodyPart bodyPart = new MimeBodyPart();
		bodyPart.setText(mBody);
		mMultiPart.addBodyPart(bodyPart);

		message.setContent(mMultiPart);

		Transport.send(message);
	}

	public String getUser() {
		return mUser;
	}

	public void setUser(String user) {
		mUser = user;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public String getSubject() {
		return mSubject;
	}

	public void setSubject(String subject) {
		mSubject = subject;
	}

	public String getSender() {
		return mSender;
	}

	public void setSender(String sender) {
		mSender = sender;
	}

	public String getRecipients() {
		return mRecipients;
	}

	public void setRecipients(String recipients) {
		mRecipients = recipients;
	}

	public String getBody() {
		return mBody;
	}

	public void setBody(String body) {
		mBody = body;
	}
}