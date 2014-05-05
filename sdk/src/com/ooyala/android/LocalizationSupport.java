package com.ooyala.android;

import java.util.HashMap;
import java.util.Map;

public final class LocalizationSupport {
	private static Map<String, Map<String, String>> defaultLocales = null;
	private static Map<String, String> currentLocalizedStrings = null;

	private static void createDefaultLocales() {
		final Map<String, String> en_US = new HashMap<String, String>();
		en_US.put("LIVE", "LIVE");
		en_US.put("Languages", "Languages");
		en_US.put("Presentation Styles", "Presentation Styles");
		en_US.put("Roll-Up", "Roll-Up");
		en_US.put("Paint-On", "Paint-On");
		en_US.put("Pop-On", "Pop-On");
		en_US.put("Done", "Done");
		en_US.put("None", "None");

		final Map<String, String> ja_JP = new HashMap<String, String>();
		ja_JP.put("LIVE", "„É©„Ç§„Éñ„Éì„Éá„Ç™");
		ja_JP.put("Languages", "言語");
		ja_JP.put("Presentation Styles", "プレゼンテーションのスタイル");
		ja_JP.put("Roll-Up", "巻き上げる");
		ja_JP.put("Paint-On", "上のペイント");
		ja_JP.put("Pop-On", "上のポップアップ表示");
		ja_JP.put("Done", "完了");
		ja_JP.put("None", "なし");

		defaultLocales = new HashMap<String, Map<String, String>>();
		defaultLocales.put("en_US", en_US);
		defaultLocales.put("ja_JP", ja_JP);
		currentLocalizedStrings = loadLocalizedStrings("en_US");
	}

	/**
	 * Loads a map of localized strings for specified locale
	 * @param localeId The ID of locale (such as en_US)
	 */
	synchronized public static Map<String, String> loadLocalizedStrings(String localeId) {
		if (defaultLocales == null) createDefaultLocales();
		return defaultLocales.get(localeId);
	}

	/**
	 * Instructs the player to use localized strings
	 */
	synchronized public static void useLocalizedStrings(Map<String, String> localizedStrings) {
		if (currentLocalizedStrings == null) createDefaultLocales();
		currentLocalizedStrings = localizedStrings;
	}

	/**
	 * Returns current localized strings
	 */
	synchronized public static String localizedStringFor(String string) {
		if (currentLocalizedStrings == null) createDefaultLocales();
		return currentLocalizedStrings.get(string);
	}
}
