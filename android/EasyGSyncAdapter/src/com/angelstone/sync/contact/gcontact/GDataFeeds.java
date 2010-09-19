package com.angelstone.sync.contact.gcontact;

import java.util.Vector;

public class GDataFeeds {
	private String id_ = null;
	private String updated_ = null;
	private String title_ = null;
	private int total_ = 0;
	private Vector<Object> entrys_ = new Vector<Object>();

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

	public int getTotal() {
		return total_;
	}

	public void setTotal(int total) {
		total_ = total;
	}

	public Vector<Object> getEntrys() {
		return entrys_;
	}

	public void setEntrys(Vector<Object> entrys) {
		entrys_ = entrys;
	}

}
