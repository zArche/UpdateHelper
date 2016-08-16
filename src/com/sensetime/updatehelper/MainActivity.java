package com.sensetime.updatehelper;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_helper_activity_main);
		UpdateHelper.newInstance(this).checkUpdate();
	}
}
