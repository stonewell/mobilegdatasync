package com.angelstone.sync.contact.gcontact;

public class GDataPhoneNumber {
	private String rel_ = null;
	private String number_ = null;

	public GDataPhoneNumber() {
	}

	public GDataPhoneNumber(String rel, String number) {
		rel_ = rel;
		number_ = number;
	}

	public String getRel() {
		return rel_;
	}

	public void setRel(String rel) {
		rel_ = rel;
	}

	public String getNumber() {
		return number_;
	}

	public void setNumber(String number) {
		number_ = number;
	}

}
