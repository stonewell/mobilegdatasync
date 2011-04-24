package com.angelstone.android.mail;

import java.util.Properties;

public class DefaultSmtpHostConfiguration implements MailHostConfiguration {

	private String mSmtpHost = "";
	private boolean mNeedAuth = false;
	private int mPort = 25;
	private boolean mNeedSSL = false;
	private int mSocketFactoryPort = 0;

	@Override
	public Properties getMailHostProperties() {
		Properties props = new Properties();
		
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mSmtpHost);
		props.put("mail.smtp.auth", mNeedAuth ? "true" : "false");
		props.put("mail.smtp.port", String.valueOf(mPort));

		if (mNeedSSL) {
			props.put("mail.smtp.socketFactory.port",
					mSocketFactoryPort > 0 ? String.valueOf(mSocketFactoryPort)
							: String.valueOf(mPort));
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
		}

		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");

		return props;
	}

	public String getSmtpHost() {
		return mSmtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		mSmtpHost = smtpHost;
	}

	public boolean isNeedAuth() {
		return mNeedAuth;
	}

	public void setNeedAuth(boolean needAuth) {
		mNeedAuth = needAuth;
	}

	public int getPort() {
		return mPort;
	}

	public void setPort(int port) {
		mPort = port;
	}

	public boolean isNeedSSL() {
		return mNeedSSL;
	}

	public void setNeedSSL(boolean needSSL) {
		mNeedSSL = needSSL;
	}

	public int getSocketFactoryPort() {
		return mSocketFactoryPort;
	}

	public void setSocketFactoryPort(int socketFactoryPort) {
		mSocketFactoryPort = socketFactoryPort;
	}

}
