package com.angelstone.sync.store;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.angelstone.sync.gclient.GDataException;
import com.angelstone.sync.log.ErrorHandler;
import com.angelstone.sync.option.Options;

public class Store {

	private static final String RECORD_STORE_NAME = "GcalSyncOptions";

	private static StoreController storeController;

	private static Options options;

	static {
		try {
			storeController = new StoreController(RECORD_STORE_NAME,
					new RecordTypes());
		} catch (Exception e) {
			ErrorHandler.showError("Failed to initailize store", e);
		}
	}

	public static Options getOptions() throws GDataException {
		if (options == null) {
			try {
				options = (Options) storeController.read(RecordTypes.OPTIONS);
			} catch (Exception e) {
				options = null;
				throw new GDataException(
						"Error reading Store Options (Store.getOptions)", e);
			}

			if (options == null) {
				options = new Options();
			}
		}
		return options;
	}

	public static void deleteRecordStore() {
		try {
			RecordStore.deleteRecordStore(RECORD_STORE_NAME);
			options = new Options();
		} catch (RecordStoreException e) {
			// Ignored
		}
	}

	public static void setOptions(Options options) {
		Store.options = options;
	}

	public static void saveOptions() {
		storeController.write(options);
	}
}
