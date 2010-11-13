package com.angelstone.android.profileswitcher.ui;

import android.view.Menu;
import android.view.MenuItem;

import com.angelstone.android.profileswitcher.R;

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
		menu.clear();

		createMenus(menu, 0, OPTION_MENUS);

		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
	
	protected abstract void editContent(int id);
	protected abstract void clearAllContent();
}
