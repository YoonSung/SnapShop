package com.l3cache.snapshop;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SnapPreference {
	private final String PREF_NAME = "com.l3cache.snapshop.pref";

	public final static String PREF_CURRENT_USER_ID = "PREF_CURRENT_USER_ID";
	public final static String PREF_CURRENT_USER_EMAIL = "PREF_CURRENT_USER_EMAIL";
	public final static String PREF_CURRENT_USER_PASSWORD = "PREF_CURRENT_USER_PASSWORD";
	public final static String PROPERTY_REG_ID = "registration_id";
	public final static String PREF_EXIF_ORIENTATION = "PREF_EXIF_ORIENTATION";

	private Context mContext;

	public SnapPreference(Context c) {
		mContext = c;
	}

	public void put(String key, String value) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putString(key, value);
		editor.commit();
	}

	public void put(String key, boolean value) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean(key, value);
		editor.commit();
	}

	public void put(String key, int value) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putInt(key, value);
		editor.commit();
	}

	public String getValue(String key, String dftValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

		try {
			return pref.getString(key, dftValue);
		} catch (Exception e) {
			return dftValue;
		}

	}

	public int getValue(String key, int dftValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

		try {
			return pref.getInt(key, dftValue);
		} catch (Exception e) {
			return dftValue;
		}

	}

	public boolean getValue(String key, boolean dftValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

		try {
			return pref.getBoolean(key, dftValue);
		} catch (Exception e) {
			return dftValue;
		}
	}

	/**
	 * 사용자가 직접 Sign Out하는 경우, 저장되어 있는 사용자 정보 삭제
	 */
	public void removeCurrentUser() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.remove(PREF_CURRENT_USER_EMAIL).remove(PREF_CURRENT_USER_ID).remove(PREF_CURRENT_USER_PASSWORD);
		editor.commit();
	}

	/**
	 * 추후 자동로그인을 위해 현재 로그인하는 사용자의 정보를 저장
	 * @param uid
	 * @param email
	 * @param password
	 */
	public void persistCurrentUser(int uid, String email, String password) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(PREF_CURRENT_USER_ID, uid);
		editor.putString(PREF_CURRENT_USER_EMAIL, email);
		editor.putString(PREF_CURRENT_USER_PASSWORD, password);
		editor.commit();
	}
}
