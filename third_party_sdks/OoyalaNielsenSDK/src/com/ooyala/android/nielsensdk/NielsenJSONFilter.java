package com.ooyala.android.nielsensdk;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import android.util.Pair;

public class NielsenJSONFilter {

  private Set<Pair<Pattern,String>> psz;

  public NielsenJSONFilter() {
    this.psz = new HashSet<Pair<Pattern,String>>();

    for( String s : new String[]{ "\\\\b", "\\\\f", "\\\\n", "\\\\r", "\\\\t" } ) {
      psz.add( new Pair<Pattern,String>( Pattern.compile( "([^\\\\])"+s ), "$1" ) );
      psz.add( new Pair<Pattern,String>( Pattern.compile( "^"+s ), "" ) );
    }

    psz.add( new Pair<Pattern,String>( Pattern.compile( "\\\\'" ), "'" ) );
  }

  public String filter( String json ) {
    String r = json;

    for( Pair<Pattern,String> ps : psz ) {
      final Matcher backspaceMatcher = ps.first.matcher( r );
      r = backspaceMatcher.replaceAll( ps.second );
      Log.v( "NielsenJSONFilter", json + " -> " + ps.first.pattern() + " -> " + r );
    }

    return r;
  }
}
