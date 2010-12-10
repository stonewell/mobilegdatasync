package com.angelstone.sync.contact.gcontact;

import java.util.Vector;

public class GDataEvent {
	private String rel_ = null;
	private Vector whens_ = new Vector();
	public String getRel() {
		return rel_;
	}
	public void setRel(String rel) {
		rel_ = rel;
	}
	public Vector getWhens() {
		return whens_;
	}
	public void setWhens(Vector whens) {
		whens_ = whens;
	}
}
