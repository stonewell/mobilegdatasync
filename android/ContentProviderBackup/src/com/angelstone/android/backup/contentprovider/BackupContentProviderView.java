package com.angelstone.android.backup.contentprovider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.angelstone.android.ui.GenericActivity;
import com.angelstone.android.utils.ASTextUtils;
import com.angelstone.android.utils.ActivityLog;

public class BackupContentProviderView extends GenericActivity {
	private boolean[] mCheckedItems = null;
	private CharSequence[] mItems = null;
	private CharSequence[] mItemNames = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setMenuEnableAbout(true);
		setMenuEnableImportExport(true);
		setMenuEnableLogs(true);
		setMenuEnablePreference(false);

		mItemNames = getResources().getTextArray(
				R.array.SupportedContentProviderNameArray);
		mItems = getResources().getTextArray(
				R.array.SupportedContentProviderUriArray);

		mCheckedItems = new boolean[mItems.length];
		for (int i = 0; i < mCheckedItems.length; i++)
			mCheckedItems[i] = true;

		final ListView lstContentProvider = (ListView) findViewById(R.id.lst_content_provider);
		lstContentProvider.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.select_dialog_multichoice,
				android.R.id.text1, mItemNames) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				if (mCheckedItems != null) {
					boolean isItemChecked = mCheckedItems[position];
					lstContentProvider.setItemChecked(position, isItemChecked);
				}
				view.setBackgroundColor(Color.WHITE);
				return view;
			}
		};

		lstContentProvider.setAdapter(adapter);
		lstContentProvider
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long id) {
						mCheckedItems[position] = !mCheckedItems[position];
						lstContentProvider.setItemChecked(position,
								mCheckedItems[position]);
					}
				});
	}

	@Override
	protected void exportTo(BufferedWriter writer) throws IOException {
		for (int i = 0; i < mCheckedItems.length; i++) {
			if (mCheckedItems[i]) {
				exportContentProvider(writer, mItems[i]);
			}
		}
	}

	private void exportContentProvider(BufferedWriter writer,
			CharSequence uriString) throws IOException {
		Uri uri = Uri.parse(uriString.toString());

		Cursor c = getContentResolver().query(uri, null, null, null, null);

		try {
			if (c == null || c.getCount() == 0) {
				ActivityLog.logWarning(this, getString(R.string.app_name),
						"no data found for uri:" + uriString);
				return;
			}

			// export uri
			writer.append("URI:").append(uriString);
			writer.newLine();

			// export name
			int count = c.getColumnCount();
			writer.append("COL:");
			for (int i = 0; i < count; i++) {
				if (i > 0)
					writer.append(",");
				writer.append(c.getColumnName(i));
			}
			writer.newLine();

			// export data
			while (c.moveToNext()) {
				for (int i = 0; i < count; i++) {
					if (i > 0)
						writer.append(",");
					String t = c.getString(i);

					if (t == null) {
						writer.append("null");
					} else {
						writer.append(ASTextUtils.toText(t.getBytes()));
					}
				}
				writer.newLine();
			}

			writer.flush();

			ActivityLog.logInfo(this, getString(R.string.app_name),
					"all data exported for uri:" + uriString);
		} finally {
			if (c != null)
				c.close();
		}
	}

	@Override
	protected int importFrom(BufferedReader br) throws IOException {
		int import_count = 0;

		String line = null;
		Uri uri = null;
		String[] col_names = null;

		HashMap<String, String> uris = new HashMap<String, String>();
		for (int i = 0; i < mCheckedItems.length; i++) {
			if (mCheckedItems[i]) {
				uris.put(mItems[i].toString(), mItems[i].toString());
			}
		}

		while ((line = br.readLine()) != null) {
			if (line.startsWith("URI:")) {
				String uriString = line.substring("URI:".length());

				uri = Uri.parse(uriString);

				if (!uris.containsKey(uriString)) {
					uri = null;
				} else {
					ActivityLog.logInfo(this, getString(R.string.app_name),
							"import uri:" + uriString);
				}
			} else if (line.startsWith("COL:")) {
				col_names = line.substring("COL:".length()).split(",");
			} else if (uri != null && col_names != null) {
				String[] valuesText = line.split(",");

				ContentValues values = new ContentValues();

				for (int i = 0; i < valuesText.length; i++) {
					if (valuesText[i].equals("null")) {
					} else {
						values.put(col_names[i],
								new String(ASTextUtils.fromText(valuesText[i])));
					}
				}

				getContentResolver().insert(uri, values);
				import_count++;
			}
		}

		if (import_count == 0)
			return IMPORT_NO_RECORD;
		return IMPORT_SUCCESS;
	}
}