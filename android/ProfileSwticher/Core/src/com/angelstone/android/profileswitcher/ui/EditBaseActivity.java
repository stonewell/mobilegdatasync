package com.angelstone.android.profileswitcher.ui;

import android.view.Menu;

import com.angelstone.android.profileswitcher.R;

public class EditBaseActivity extends GenericActivity {
	private static final int[][] OPTION_MENUS = new int[][] {
			new int[] { R.string.save, android.R.drawable.ic_menu_save },
			new int[] { R.string.delete, android.R.drawable.ic_menu_delete }, };

	private int mOptionMenuStartPos = 0;
	
	public EditBaseActivity() {
		super(OPTION_MENUS.length);
	}

	public EditBaseActivity(int optionMenuStartPos) {
		super(optionMenuStartPos + OPTION_MENUS.length);
		
		mOptionMenuStartPos = optionMenuStartPos;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mOptionMenuStartPos == 0)
			menu.clear();

		createMenus(menu, 0, OPTION_MENUS);

		return super.onPrepareOptionsMenu(menu);
	}

}
