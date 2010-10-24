package com.angelstone.sync.store;

public class StoreException extends RuntimeException {

	private Throwable reason;

	public StoreException(String string) {
		super(string);
	}

	public StoreException(String string, Throwable reason) {
		super(string);
		this.reason = reason;
	}

	public Throwable getReason() {
		return reason;
	}
}
