package com.angelstone.sync.contact.gcontact;

import java.util.Vector;

public class GDataEvent {
	private String rel_ = null;
	private Vector<Object> whens_ = new Vector<Object>();
	public String getRel() {
		return rel_;
	}
	public void setRel(String rel) {
		rel_ = rel;
	}
	public Vector<Object> getWhens() {
		return whens_;
	}
	public void setWhens(Vector<Object> whens) {
		whens_ = whens;
	}
}
