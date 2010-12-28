package com.angelstone.android.profileswitcher.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.ui.GenericActivity;

public abstract class ContentListBaseActivity extends GenericActivity {
	private static final int[][] OPTION_MENUS = new int[][] {
			new int[] { R.string.add, android.R.drawable.ic_menu_add },
			new int[] { R.string.clear_all, android.R.drawable.ic_menu_delete }, };

	public ContentListBaseActivity() {
		super(OPTION_MENUS.length);
	}

	public ContentListBaseActivity(int optionMenuStartPos) {
		super(optionMenuStartPos + OPTION_MENUS.length);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (getOptionMenuStartPos() - OPTION_MENUS.length == 0)
			menu.clear();

		createMenus(menu, getOptionMenuStartPos() - OPTION_MENUS.length, OPTION_MENUS);

		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId() - getOptionMenuStartPos() + OPTION_MENUS.length) {
		case 0: {
			editContent(-1);
			break;
		}
		case 1: {
			clearAllContent();
			break;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo menuInfo = item.getMenuInfo();
		long childId = -1;

		if (menuInfo instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			childId = info.id;
		} else {
			return false;
		}

		if (childId <= 0)
			return false;
		
		final long id = childId;
		switch (item.getItemId()) {
		case 1: {
			editContent(id);
			break;
		}
		case 2: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.delete_confirm)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									deleteContent(id);
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create();
			ad.show();
			break;

		}
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.add(0, 1, 1, R.string.edit);
		menu.add(0, 2, 2, R.string.delete);

		super.onCreateContextMenu(menu, v, menuInfo);

	}
	
	protected abstract void editContent(long id);
	protected abstract void clearAllContent();
	protected abstract void deleteContent(long id);
}
