package com.angelstone.sync.contact.gcontact;

import java.util.Hashtable;
import java.util.Vector;

public class GDataEntry {
	private String id_ = null;
	private String updated_ = null;
	
	private String title_ = null;
	private String fullName_ = null;
	private String givenName_ = null;
	private String familyName_ = null;
	
	private String birthday_ = null;
	
	private String category_ = null;
	private Hashtable<String, String> links_ = new Hashtable<String, String>();

	private Vector<Object> emails_ = new Vector<Object>();
	private String primaryEmail_ = null;

	private Vector<Object> phoneNumbers_ = new Vector<Object>();
	private Vector<Object> groups_ = new Vector<Object>();
	private String content_ = null;
	private String systemGroup_ = null;
	
	private Vector<Object> events_ = new Vector<Object>();
	
	private Vector<Object> structuredPostalAddresses_ = new Vector<Object>();

	private Vector<Object> organizations_ = new Vector<Object>();
	
	public boolean isGroup() {
		return (category_ != null && category_.endsWith("#group"));
	}

	public boolean isContact() {
		return (category_ != null && category_.endsWith("#contact"));
	}

	public boolean isSystemGroup() {
		return isGroup() && systemGroup_ != null;
	}

	public Vector<Object> getEmails() {
		return emails_;
	}

	public void setEmails(Vector<Object> emails) {
		emails_ = emails;
	}

	public String getPrimaryEmail() {
		return primaryEmail_;
	}

	public void setPrimaryEmail(String primaryEmail) {
		primaryEmail_ = primaryEmail;
	}

	public Vector<Object> getPhoneNumbers() {
		return phoneNumbers_;
	}

	public void setPhoneNumbers(Vector<Object> phoneNumbers) {
		phoneNumbers_ = phoneNumbers;
	}

	public String getId() {
		return id_;
	}

	public void setId(String id) {
		id_ = id;
	}

	public String getUpdated() {
		return updated_;
	}

	public void setUpdated(String updated) {
		updated_ = updated;
	}

	public String getTitle() {
		return title_;
	}

	public void setTitle(String title) {
		title_ = title;
	}

	public String getCategory() {
		return category_;
	}

	public void setCategory(String category) {
		category_ = category;
	}

	public Hashtable<String, String> getLinks() {
		return links_;
	}

	public void setLinks(Hashtable<String, String> links) {
		links_ = links;
	}

	public Vector<Object> getGroups() {
		return groups_;
	}

	public void setGroups(Vector<Object> groups) {
		groups_ = groups;
	}

	public String getContent() {
		return content_;
	}

	public void setContent(String content) {
		content_ = content;
	}

	public String getSystemGroup() {
		return systemGroup_;
	}

	public void setSystemGroup(String sytemGroup) {
		systemGroup_ = sytemGroup;
	}

	public String getFullName() {
		return fullName_;
	}

	public void setFullName(String fullName) {
		fullName_ = fullName;
	}

	public String getGivenName() {
		return givenName_;
	}

	public void setGivenName(String givenName) {
		givenName_ = givenName;
	}

	public String getFamilyName() {
		return familyName_;
	}

	public void setFamilyName(String familyName) {
		familyName_ = familyName;
	}

	public String getBirthday() {
		return birthday_;
	}

	public void setBirthday(String birthday) {
		birthday_ = birthday;
	}

	public Vector<Object> getEvents() {
		return events_;
	}

	public void setEvents(Vector<Object> events) {
		events_ = events;
	}

	public Vector<Object> getStructuredPostalAddresses() {
		return structuredPostalAddresses_;
	}

	public void setStructuredPostalAddresses(Vector<Object> structuredPostalAddresses) {
		structuredPostalAddresses_ = structuredPostalAddresses;
	}

	public Vector<Object> getOrganizations() {
		return organizations_;
	}

	public void setOrganizations(Vector<Object> organizations) {
		organizations_ = organizations;
	}
}
