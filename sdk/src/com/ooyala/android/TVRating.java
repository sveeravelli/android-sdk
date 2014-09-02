package com.ooyala.android;

import java.util.Arrays;

import android.text.TextUtils;

public class TVRating {
  public final String ageRestriction;
  public final String labels;
  public final String clickthrough;
  
  public TVRating( String ageRestriction, String labels, String clickthrough ) {
    
    if( ageRestriction != null ) {
      ageRestriction = ageRestriction.toUpperCase().replace( "TV-", "" );
    }
    this.ageRestriction = ageRestriction;
    
    if( labels != null ) {
      labels = labels.toUpperCase().replace( ",", " " ).replace( ";", " " );
      String[] labelsArray = labels.split( "\\s+" );
      Arrays.sort( labelsArray, String.CASE_INSENSITIVE_ORDER );
      labels = TextUtils.join( "", labelsArray );
    }
    this.labels = labels;
    
    this.clickthrough = clickthrough;
  }
}
