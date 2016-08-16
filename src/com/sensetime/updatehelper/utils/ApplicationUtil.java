package com.sensetime.updatehelper.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class ApplicationUtil {

	public static String getVersionName(Context context) {
		String versionName = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public static String getVersionCode(Context context) {
		String versionCode = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionCode = info.versionCode + "";
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	public static String getApplicationName(Context context) {
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return (String) packageManager.getApplicationLabel(applicationInfo);
	}

	public static String getAppPackageName(Context context) {
		return context.getPackageName();
	}

	public static String getPackageNameOfApk(Context context, String apkPath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
		ApplicationInfo appInfo = null;
		if (info != null) {
			appInfo = info.applicationInfo;
			return appInfo.packageName;
		}
		return null;
	}

}
