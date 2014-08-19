package com.ooyala.android;

import java.util.Arrays;

import android.text.TextUtils;

public class TVRatings {
  public final String rating;
  public final String labels;
  public final String clickthrough;
  
  public TVRatings( String rating, String labels, String clickthrough ) {
    
    if( rating != null ) {
      rating = rating.toUpperCase().replace( "TV-", "" );
    }
    this.rating = rating;
    
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
