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
	private Hashtable links_ = new Hashtable();

	private Vector emails_ = new Vector();
	private String primaryEmail_ = null;

	private Vector phoneNumbers_ = new Vector();
	private Vector groups_ = new Vector();
	private String content_ = null;
	private String systemGroup_ = null;
	
	private Vector events_ = new Vector();
	
	private Vector structuredPostalAddresses_ = new Vector();

	private Vector organizations_ = new Vector();
	
	public boolean isGroup() {
		return (category_ != null && category_.endsWith("#group"));
	}

	public boolean isContact() {
		return (category_ != null && category_.endsWith("#contact"));
	}

	public boolean isSystemGroup() {
		return isGroup() && systemGroup_ != null;
	}

	public Vector getEmails() {
		return emails_;
	}

	public void setEmails(Vector emails) {
		emails_ = emails;
	}

	public String getPrimaryEmail() {
		return primaryEmail_;
	}

	public void setPrimaryEmail(String primaryEmail) {
		primaryEmail_ = primaryEmail;
	}

	public Vector getPhoneNumbers() {
		return phoneNumbers_;
	}

	public void setPhoneNumbers(Vector phoneNumbers) {
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

	public Hashtable getLinks() {
		return links_;
	}

	public void setLinks(Hashtable links) {
		links_ = links;
	}

	public Vector getGroups() {
		return groups_;
	}

	public void setGroups(Vector groups) {
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

	public Vector getEvents() {
		return events_;
	}

	public void setEvents(Vector events) {
		events_ = events;
	}

	public Vector getStructuredPostalAddresses() {
		return structuredPostalAddresses_;
	}

	public void setStructuredPostalAddresses(Vector structuredPostalAddresses) {
		structuredPostalAddresses_ = structuredPostalAddresses;
	}

	public Vector getOrganizations() {
		return organizations_;
	}

	public void setOrganizations(Vector organizations) {
		organizations_ = organizations;
	}
}
