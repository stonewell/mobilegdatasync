package com.angelstone.easygsync.contacts;


public class ContactsSync {
	private String userName_ = null;
	private String password_ = null;

	public ContactsSync() {

	}

	public ContactsSync(String userName, String password) {
		userName_ = userName;
		password_ = password;
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
