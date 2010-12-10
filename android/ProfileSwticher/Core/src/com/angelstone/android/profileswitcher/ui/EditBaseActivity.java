package com.angelstone.android.profileswitcher.ui;

import android.view.Menu;
import android.view.MenuItem;

import com.angelstone.android.profileswitcher.R;

public abstract class EditBaseActivity extends GenericActivity {
	private static final int[][] OPTION_MENUS = new int[][] {
			new int[] { R.string.save, android.R.drawable.ic_menu_save },
			new int[] { R.string.delete, android.R.drawable.ic_menu_delete }, };

	public EditBaseActivity() {
		super(OPTION_MENUS.length);
	}

	public EditBaseActivity(int optionMenuStartPos) {
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
			saveContent();
			break;
		}
		case 1: {
			deleteContent();
			break;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	protected abstract void saveContent();
	protected abstract void deleteContent();
}
