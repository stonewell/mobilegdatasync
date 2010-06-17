package com.angelstone.sync.contact.gcontact;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.HttpsConnection;


import com.angelstone.sync.gclient.GDataClient;
import com.angelstone.sync.gclient.GDataException;
import com.primosync.cal.gcal.GCalClient;
import com.primosync.util.DateUtil;
import com.primosync.util.HttpUtil;

public class GContactClient extends GDataClient {
	public static final String DEFAULT_CONTACTS_URL_PREFIX = "http://www.google.com/m8/feeds/contacts/";
	public static final String DEFAULT_GROUPS_URL_PREFIX = "http://www.google.com/m8/feeds/groups/";

	private GDataLoginResult result_ = null;
	private int version = 3;

	public String login(String userName, String password) {
		setCred(userName, password);

		return login();
	}

	public String login() {
		result_ = login(GDataClient.CONTACTS_SERVICE);

		return result_.errorMsg;
	}

	public boolean isAuthorized() {
		return result_ != null && result_.isAuthorized;
	}

	public GDataFeeds downloadGroups(boolean fullDownload, long lastUpdate)
			throws GDataException {

		if (!isAuthorized())
			return null;

		try {
			byte[] feedsBytes = null;

			String groupsUrl = DEFAULT_CONTACTS_URL_PREFIX + username
					+ "%40gmail.com" + "/full";

			// Prepare parameters
			String parameters = "?v=" + version + "&max-results=1000000";

			if (!fullDownload && lastUpdate != 0) {
				String lastSyncTime = DateUtil.longToIsoDateTime(lastUpdate);
				parameters += "&updated-min=" + lastSyncTime;
			}

			groupsUrl += parameters;

			feedsBytes = HttpUtil.sendRequest(groupsUrl, HttpsConnection.GET,
					null, result_.authorizationHeader);

			if (feedsBytes != null) {
				GDataFeedsParser parser = GDataFeedsParserFactory
						.createParser(version);
				GDataFeeds contacts = parser.parseFeeds(feedsBytes);

				if (contacts.getId() != null)
					return contacts;

				throw new GDataFeedsException(new String(feedsBytes),
						"GContactClient", "downloadGroups");
			}

			return null;
		} catch (IOException e) {
			throw new GDataFeedsException(this.getClass(), "downloadGroups", e);
		}
	}

	public GDataFeeds downloadContacts(boolean fullDownload, long lastUpdate,
			Vector groups) throws GDataException {

		if (!isAuthorized())
			return null;

		try {
			byte[] feedsBytes = null;

			String contactsUrl = DEFAULT_CONTACTS_URL_PREFIX + username
					+ "%40gmail.com" + "/full";

			// Prepare parameters
			String parameters = "?v=" + version + "&max-results=1000000";

			if (!fullDownload && lastUpdate != 0) {
				String lastSyncTime = DateUtil.longToIsoDateTime(lastUpdate);
				parameters += "&updated-min=" + lastSyncTime;
			}

			if (groups != null && groups.size() > 0) {
				Enumeration it = groups.elements();

				while (it.hasMoreElements()) {
					parameters += "&group=" + it.nextElement();
				}
			}

			update("Downloading Contacts list...");

			contactsUrl += parameters;

			feedsBytes = HttpUtil.sendRequest(contactsUrl, HttpsConnection.GET,
					null, result_.authorizationHeader);

			if (feedsBytes != null) {
				GDataFeedsParser parser = GDataFeedsParserFactory
						.createParser(version);
				GDataFeeds contacts = parser.parseFeeds(feedsBytes);

				if (contacts.getId() != null)
					return contacts;

				throw new GDataFeedsException(new String(feedsBytes),
						"GContactClient", "downloadContacts");
			}

			return null;
		} catch (IOException e) {
			throw new GDataFeedsException(this.getClass(), "downloadContacts",
					e);
		}
	}

}
