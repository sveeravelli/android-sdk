package com.ooyala.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import com.ooyala.android.OoyalaPlayer;

public class SelectedLanguageId {

  private static final String OO_SELECTED_LANGUAGE_STRING = "oo_selected_language_string";

  private Context context;
  private String selectedLanguageString;

  public SelectedLanguageId( Context context ) {
    this.context = context;
    this.selectedLanguageString = loadSelectedLanguageString( context );
  }

  /**
   * @return possibly null.
   */
  public String get() {
    return selectedLanguageString;
  }

  public void set( String string ) {
    selectedLanguageString = string;
    saveSelectedLanguageString( context, selectedLanguageString );
  }

  private static String loadSelectedLanguageString( Context context ) {
    SharedPreferences preferences = context.getSharedPreferences( OoyalaPlayer.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS );
    String string = preferences.getString( OO_SELECTED_LANGUAGE_STRING, null );
    return string;
  }

  private static void saveSelectedLanguageString( Context context, String string ) {
    SharedPreferences preferences = context.getSharedPreferences( OoyalaPlayer.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS );
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString( OO_SELECTED_LANGUAGE_STRING, string );
    editor.commit();
  }
}
