package com.angelstone.sync.option;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SyncOptions {

	public static final int LATEST_CHANGE_WINS_MERGE = 1;
	public static final int GDATA_WINS_MERGE = 2;
	public static final int PHONE_WINS_MERGE = 3;

	public String username = "";
	public String password = "";
	public boolean upload = true;
	public boolean download = true;
	public boolean autoLogin = false;
	public int mergeStrategy = LATEST_CHANGE_WINS_MERGE;
	public long downloadTimeZoneOffset = 0;
	public long uploadTimeZoneOffset = 0;
	public boolean preview = true;
	public int autosyncTime = 30; // time between autosync in minutes

	static final int DEFAULT_RECORD_VERSION = 2;

	public SyncOptions() {
	}

	public void readRecord(DataInputStream in) throws IOException {
		int version;

		// read record version
		version = in.readInt();

		if (version >= 1) {
			username = in.readUTF();
			password = in.readUTF();

			downloadTimeZoneOffset = in.readLong();
			uploadTimeZoneOffset = in.readLong();
			upload = in.readBoolean();
			download = in.readBoolean();
			autoLogin = in.readBoolean();
		}

		if (version >= 2) {
			autosyncTime = in.readInt();
		}
	}

	public void writeRecord(DataOutputStream out) throws IOException {
		out.writeInt(DEFAULT_RECORD_VERSION);

		out.writeUTF(username);
		out.writeUTF(password);

		out.writeLong(downloadTimeZoneOffset);
		out.writeLong(uploadTimeZoneOffset);
		out.writeBoolean(upload);
		out.writeBoolean(download);
		out.writeBoolean(autoLogin);

		// version 2
		out.writeInt(autosyncTime);
	}
	
	private static final SyncOptions instance_ = 
		new SyncOptions();
	
	public static final SyncOptions getInstance() {
		return instance_;
	}
}
