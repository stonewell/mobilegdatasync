package com.angelstone.android.profileswitcher.ui.widgets;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angelstone.android.profileswitcher.R;

public class KindSectionView extends LinearLayout implements OnClickListener {
	private static final String TAG = "KindSectionView";

	private LayoutInflater mInflater;

	private ViewGroup mEditors;
	private View mAdd;
	private TextView mTitle;

	public KindSectionView(Context context) {
		super(context);
	}

	public KindSectionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/** {@inheritDoc} */
	@Override
	protected void onFinishInflate() {
		mInflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);

		setDrawingCacheEnabled(true);
		setAlwaysDrawnWithCacheEnabled(true);

		mEditors = (ViewGroup) findViewById(R.id.kind_editors);

		mAdd = findViewById(R.id.kind_header);
		mAdd.setOnClickListener(this);

		mTitle = (TextView) findViewById(R.id.kind_title);
	}

	/** {@inheritDoc} */
	public void onDeleted(Editor editor) {
		this.updateAddEnabled();
		this.updateEditorsVisible();
	}

	/** {@inheritDoc} */
	public void onRequest(int request) {
		// Ignore requests
	}

	// public void setState(DataKind kind, EntityDelta state, boolean readOnly)
	// {
	// mKind = kind;
	// mState = state;
	// mReadOnly = readOnly;
	//
	// // TODO: handle resources from remote packages
	// mTitle.setText(kind.titleRes);
	//
	// this.rebuildFromState();
	// this.updateAddEnabled();
	// this.updateEditorsVisible();
	// }
	//
	// /**
	// * Build editors for all current {@link #mState} rows.
	// */
	// public void rebuildFromState() {
	// // Remove any existing editors
	// mEditors.removeAllViews();
	//
	// // Build individual editors for each entry
	// if (!mState.hasMimeEntries(mKind.mimeType)) return;
	// for (ValuesDelta entry : mState.getMimeEntries(mKind.mimeType)) {
	// // Skip entries that aren't visible
	// if (!entry.isVisible()) continue;
	//
	// final GenericEditorView editor = (GenericEditorView)mInflater.inflate(
	// R.layout.item_generic_editor, mEditors, false);
	// editor.setValues(mKind, entry, mState, mReadOnly);
	// editor.setEditorListener(this);
	// editor.setId(entry.getViewId());
	// mEditors.addView(editor);
	// }
	// }

	protected void updateEditorsVisible() {
		final boolean hasChildren = mEditors.getChildCount() > 0;
		mEditors.setVisibility(hasChildren ? View.VISIBLE : View.GONE);
	}

	protected void updateAddEnabled() {
		// Set enabled state on the "add" view
//		final boolean canInsert = EntityModifier.canInsert(mState, mKind);
//		final boolean isEnabled = !mReadOnly && canInsert;
		mAdd.setEnabled(true);
	}

	/** {@inheritDoc} */
	public void onClick(View v) {
		// Insert a new child and rebuild
//		EntityModifier.insertChild(mState, mKind);
//		this.rebuildFromState();
		this.updateAddEnabled();
		this.updateEditorsVisible();
	}
}
