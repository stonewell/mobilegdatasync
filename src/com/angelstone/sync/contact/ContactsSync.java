package com.angelstone.sync.contact;

import harmony.java.util.StringTokenizer;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;


import com.angelstone.sync.contact.gcontact.GContactClient;
import com.angelstone.sync.contact.gcontact.GDataEntry;
import com.angelstone.sync.contact.gcontact.GDataFeeds;
import com.angelstone.sync.contact.gcontact.GDataPhoneNumber;
import com.angelstone.sync.gclient.GDataException;
import com.primosync.log.ErrorHandler;

public class ContactsSync {
	private String userName_ = null;
	private String password_ = null;

	public ContactsSync() {

	}

	public ContactsSync(String userName, String password) {
		userName_ = userName;
		password_ = password;
	}

	public void syncContacts() {
		try {
			GContactClient gContactClient = new GContactClient();
			gContactClient.login(userName_, password_);

			if (gContactClient.isAuthorized()) {
				GDataFeeds groupFeeds = gContactClient.downloadGroups(true, 0);

				Vector groups = new Vector();
				groups.addElement(findMyContactsGroup(groupFeeds));

				GDataFeeds contacts = gContactClient.downloadContacts(false, 0,
						groups);

				syncContacts(groupFeeds, contacts);
			}
		} catch (Throwable t) {
			ErrorHandler.showError(t);
		}
	}

	private String findMyContactsGroup(GDataFeeds groupFeeds) {
		Enumeration groups = groupFeeds.getEntrys().elements();

		while (groups.hasMoreElements()) {
			GDataEntry gEntry = (GDataEntry) groups.nextElement();

			if (!gEntry.isGroup())
				continue;

			String systemGroup = gEntry.getSystemGroup();

			if ("Contacts".equals(systemGroup))
				return gEntry.getId();
		}

		return "http://www.google.com/m8/feeds/groups/" + userName_
				+ "%40gmail.com/base/6";
	}

	public void syncContacts(GDataFeeds groups, GDataFeeds contacts)
			throws GDataException {
		try {
			PIM pim = PIM.getInstance();

			ContactList phoneContacts = (ContactList) pim.openPIMList(
					PIM.CONTACT_LIST, PIM.READ_WRITE);

			Enumeration cEntries = contacts.getEntrys().elements();

			while (cEntries.hasMoreElements()) {
				GDataEntry cEntry = (GDataEntry) cEntries.nextElement();

				if (!cEntry.isContact())
					continue;

				Contact c = phoneContacts.createContact();

				mergeName(phoneContacts, cEntry, c);

				// Content
				mergeContent(phoneContacts, cEntry, c);

				// Birthday
				mergeBirthday(phoneContacts, cEntry, c);

				// Phone Numbers
				mergePhoneNumber(phoneContacts, cEntry, c);

				c.commit();
			}

			if (phoneContacts != null)
				phoneContacts.close();
		} catch (PIMException e) {
			throw new GDataException(getClass(), "syncContacts", e);
		}
	}

	private void mergePhoneNumber(ContactList phoneContacts, GDataEntry cEntry,
			Contact c) {
		if (!phoneContacts.isSupportedField(Contact.TEL))
			return;

		Enumeration phoneKeys = cEntry.getPhoneNumbers().elements();

		while (phoneKeys.hasMoreElements()) {
			GDataPhoneNumber pn = (GDataPhoneNumber) phoneKeys.nextElement();

			if (pn.getRel() == null || pn.getNumber() == null)
				continue;

			if (pn.getRel().endsWith("#home")) {
				if (phoneContacts.isSupportedArrayElement(Contact.TEL,
						Contact.ATTR_HOME))
					c.addString(Contact.TEL, Contact.ATTR_HOME, pn.getNumber());
			} else if (pn.getRel().endsWith("#work")) {
				if (phoneContacts.isSupportedArrayElement(Contact.TEL,
						Contact.ATTR_WORK))
					c.addString(Contact.TEL, Contact.ATTR_WORK, pn.getNumber());
			} else if (pn.getRel().endsWith("#mobile")) {
				if (phoneContacts.isSupportedArrayElement(Contact.TEL,
						Contact.ATTR_MOBILE))
					c.addString(Contact.TEL, Contact.ATTR_MOBILE, pn
							.getNumber());
			}
		}
	}

	private void mergeBirthday(ContactList phoneContacts, GDataEntry cEntry,
			Contact c) {
		if (!phoneContacts.isSupportedField(Contact.BIRTHDAY))
			return;

		if (cEntry.getBirthday() != null) {
			StringTokenizer st = new StringTokenizer(cEntry.getBirthday(), "-");

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Integer.valueOf(st.nextToken()).intValue());
			cal.set(Calendar.MONTH, Integer.valueOf(st.nextToken()).intValue());
			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(st.nextToken())
					.intValue());

			c.addDate(Contact.BIRTHDAY, Contact.ATTR_NONE, cal.getTime()
					.getTime());
		}
	}

	private void mergeContent(ContactList phoneContacts, GDataEntry cEntry,
			Contact c) {
		if (!phoneContacts.isSupportedField(Contact.NOTE))
			return;

		if (cEntry.getContent() != null)
			c.addString(Contact.NOTE, Contact.ATTR_NONE, cEntry.getContent());
	}

	private void mergeName(ContactList phoneContacts, GDataEntry cEntry,
			Contact c) {
		if (!phoneContacts.isSupportedField(Contact.NAME))
			return;

		if (!phoneContacts.isSupportedArrayElement(Contact.NAME,
				Contact.NAME_FAMILY))
			return;

		if (!phoneContacts.isSupportedArrayElement(Contact.NAME,
				Contact.NAME_GIVEN))
			return;

		// Name
		String[] name = new String[phoneContacts.stringArraySize(Contact.NAME)];
		name[Contact.NAME_GIVEN] = getFirstName(cEntry);
		name[Contact.NAME_FAMILY] = getLastName(cEntry);
		c.addStringArray(Contact.NAME, Contact.ATTR_NONE, name);
	}

	private String getLastName(GDataEntry cEntry) {
		if (cEntry.getFamilyName() != null)
			return cEntry.getFamilyName();

		if (cEntry.getFullName() != null)
			return getLastName(cEntry.getFullName());

		return getLastName(cEntry.getTitle());
	}

	private String getFirstName(GDataEntry cEntry) {
		if (cEntry.getGivenName() != null)
			return cEntry.getGivenName();

		if (cEntry.getFullName() != null)
			return getFirstName(cEntry.getFullName());

		return getFirstName(cEntry.getTitle());
	}

	private String getLastName(String title) {
		if (title.indexOf(" ") > 0) {
			int index = title.indexOf(" ");

			return title.substring(index).trim();
		}
		return title.substring(0, 1);
	}

	private String getFirstName(String title) {
		if (title.indexOf(" ") > 0) {
			int index = title.indexOf(" ");

			return title.substring(0, index).trim();
		}
		return title.substring(1);
	}

	public String getUserName() {
		return userName_;
	}

	public void setUserName(String userName) {
		userName_ = userName;
	}

	public String getPassword() {
		return password_;
	}

	public void setPassword(String password) {
		password_ = password;
	}
}
