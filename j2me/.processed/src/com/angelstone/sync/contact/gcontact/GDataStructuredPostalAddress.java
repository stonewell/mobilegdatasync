package com.angelstone.sync.contact.gcontact;

public class GDataStructuredPostalAddress {
	private String formattedAddress_ = null;
	private String rel_ = null;
	private String street_ = null;
	private String postcode_ = null;

	public String getFormattedAddress() {
		return formattedAddress_;
	}

	public void setFormattedAddress(String formattedAddress) {
		formattedAddress_ = formattedAddress;
	}

	public String getRel() {
		return rel_;
	}

	public void setRel(String rel) {
		rel_ = rel;
	}

	public String getStreet() {
		return street_;
	}

	public void setStreet(String street) {
		street_ = street;
	}

	public String getPostcode() {
		return postcode_;
	}

	public void setPostcode(String postcode) {
		postcode_ = postcode;
	}
}
