package com.ooyala.android;

import java.util.Arrays;

public class TVRatings {
  public final String rating;
  public final String labels;
  
  public TVRatings( String rating, String labels ) {
    String fixedRating = rating.toUpperCase().replace( "TV-", "" );
    this.rating = fixedRating;
    
    String fixedLabels = labels.toUpperCase().replace( ",", " " ).replace( ";", " " );
    String[] labelsArray = fixedLabels.split( "\\w+" );
    Arrays.sort( labelsArray, String.CASE_INSENSITIVE_ORDER );
    this.labels = fixedLabels;
    
  }
}
