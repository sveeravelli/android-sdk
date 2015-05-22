package com.ooyala.android.nielsensdk;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Pair;

public class NielsenJSONFilter {

  public static final NielsenJSONFilter s_instance = new NielsenJSONFilter();

  private Set<Pair<Pattern,String>> psz;

  /**
   * Handle invalid spaces of strings and put them to the set.
   */
  public NielsenJSONFilter() {
    this.psz = new HashSet<Pair<Pattern,String>>();

    for( String s : new String[]{ "\\\\b", "\\\\f", "\\\\n", "\\\\r", "\\\\t" } ) {
      psz.add( new Pair<Pattern,String>( Pattern.compile( "([^\\\\])"+s ), "$1" ) );
      psz.add( new Pair<Pattern,String>( Pattern.compile( "^"+s ), "" ) );
    }

    psz.add( new Pair<Pattern,String>( Pattern.compile( "\\\\'" ), "'" ) );
  }

  /**
   * Replace and filter unnecessary strings in the set.
   * @param json the string to be filtered
   * @return the filtered string
   */
  public String filter( String json ) {
    String r = json;
    if( r != null ) {
      for( Pair<Pattern, String> ps : psz ) {
        final Matcher backspaceMatcher = ps.first.matcher( r );
        r = backspaceMatcher.replaceAll( ps.second );
      }
    }
    return r;
  }
}
