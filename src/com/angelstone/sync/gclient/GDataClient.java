package com.angelstone.sync.gclient;

import javax.microedition.io.HttpsConnection;
import javax.microedition.lcdui.Form;

import com.angelstone.sync.utils.HttpsUtil;

public class GDataClient {
	protected static class GDataLoginResult {
		public String authorizationHeader;
		public String errorMsg;
		public boolean isAuthorized;
	};

	protected static final String LOGIN_URL = "https://www.google.com/accounts/ClientLogin";

	public static final String CONTACTS_SERVICE = "cp";
	public static final String CALENDER_SERVICE = "cl";

	protected String username = "";
	protected String password = "";

	protected Form form;

	public GDataClient() {
		setForm(null);
	}

	public GDataClient(Form form) {
		setForm(form);
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public void setCred(String usernm, String passwd) {
		username = usernm;
		password = passwd;
	}

	protected GDataLoginResult login(String service, String usernm,
			String passwd) {
		setCred(usernm, passwd);
		return login(service);
	}

	protected GDataLoginResult login(String service) {

		GDataLoginResult ret = new GDataLoginResult();

		if (username.trim().equals("") || password.trim().equals(""))
			throw new IllegalArgumentException("Username or password is blank");

		// update("Logging in...");
		String parameters = "Email=" + username + "&Passwd=" + password
				+ "&source=Zenior-GCalSync-1&service=" + service
				+ "&accountType=HOSTED_OR_GOOGLE";

		byte[] result = HttpsUtil.sendRequest(LOGIN_URL, HttpsConnection.POST,
				parameters, null);

		String loginResponse = new String(result);
		int lastResponseCode = HttpsUtil.getLastResponseCode();
		if (lastResponseCode == HttpsConnection.HTTP_OK) {
			ret.authorizationHeader = getAuthCode(loginResponse);
			ret.isAuthorized = true;
			// no error
			ret.errorMsg = null;
		} else {
			ret.isAuthorized = false;
			ret.authorizationHeader = null;
			ret.errorMsg = "ERR: (" + lastResponseCode + ") "
					+ HttpsUtil.getLastResponseMsg();
		}

		return ret;
	}

	private String getAuthCode(String response) {
		String authCode = null;
		int authPosition = response.indexOf("Auth=");
		if (authPosition > 0) {
			int newLinePosition = response.indexOf('\n', authPosition);
			if (newLinePosition > 0) {
				authCode = "GoogleLogin auth="
						+ response.substring(authPosition + 5, newLinePosition);
			} else {
				authCode = "GoogleLogin auth="
						+ response.substring(authPosition + 5);
			}
		}
		return authCode;
	}

	/**
	 * Encodes all colons in given string (with %3a)
	 * 
	 * @param in
	 *            string to search
	 * @returns new string containing encoded colons
	 */
	protected String encode(String in) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (c == ':') {
				// found colon...append "%3a"
				sb.append("%3a");
			} else {
				// append any other character
				sb.append(c);
			}
		}

		return sb.toString();
	}

	protected void update(String message) {
		if (form != null) {
			form.append(message);
		}
	}
}
