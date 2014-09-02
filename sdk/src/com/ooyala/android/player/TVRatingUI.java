package com.ooyala.android.player;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ooyala.android.TVRating;
import com.ooyala.android.configuration.TVRatingConfiguration;
import com.ooyala.android.ui.FCCTVRatingView;

public class TVRatingUI {

  public static final int TVRATING_PLAYHEAD_TIME_MINIMUM = 250;
  private static final int MARGIN_DIP = 5;
  private ViewGroup _parentLayout;
  private RelativeLayout _relativeLayout;
  private View _videoView;
  private FCCTVRatingView _tvRatingView;

  public TVRatingUI( View videoView, ViewGroup parentLayout, TVRatingConfiguration tvRatingConfiguration ) {
    this._videoView = videoView;
    this._parentLayout = parentLayout;
    Context context = this._parentLayout.getContext();
    /*
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/movie_layout"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:background="#00000000" >
     */
    this._relativeLayout = new RelativeLayout( context );
    this._relativeLayout.setBackgroundColor( android.graphics.Color.TRANSPARENT );
    /*
        <com.ooyala.android.player.MovieView
            android:id="@+id/movie_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:background="#FF000000" />
     */
    if( this._videoView.getId() == View.NO_ID ) {
      this._videoView.setId( getUnusedId( this._parentLayout ) );
    }
    this._videoView.setBackgroundColor( android.graphics.Color.BLACK );
    RelativeLayout.LayoutParams paramsForMovieView = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
    paramsForMovieView.addRule( RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE );
    this._relativeLayout.addView( this._videoView, paramsForMovieView );
    /*
        <com.ooyala.android.ui.FCCTVRatingView
            android:id="@+id/TVRating_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_alignTop="@id/movie_view"
            android:layout_alignLeft="@id/movie_view"
            android:layout_alignBottom="@id/movie_view"
            android:layout_alignRight="@id/movie_view"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginTop="5dp"
            android:visibility="invisible"
            android:background="#00000000" />
     */
    this._tvRatingView = new FCCTVRatingView( context );
    this._tvRatingView.setVisibility( View.INVISIBLE );
    this._tvRatingView.setBackgroundColor( android.graphics.Color.TRANSPARENT );
    RelativeLayout.LayoutParams paramsForRatingsView = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_TOP, this._videoView.getId() );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_LEFT, this._videoView.getId() );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_BOTTOM, this._videoView.getId() );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_RIGHT, this._videoView.getId() );
    int margin = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, MARGIN_DIP, context.getResources().getDisplayMetrics() );
    paramsForRatingsView.setMargins( margin, margin, margin, margin ); 
    this._relativeLayout.addView( this._tvRatingView, paramsForRatingsView );
    this._tvRatingView.setTVRatingConfiguration( tvRatingConfiguration );
    
    FrameLayout.LayoutParams paramsForRelative = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT );
    this._parentLayout.addView( this._relativeLayout, paramsForRelative );
  }
  
  public void pushTVRating( TVRating tvRating ) {
    _tvRatingView.setTVRating( tvRating );
  }

  private int getUnusedId( ViewGroup parentLayout ) {
    // i don't know if ids are generated up or down, and
    // i've heard that some regions of int values are reserved.
    // and empirically negative values didn't work.
    // so all in all, i'm doing this heuristic.
    Set<Integer> seenIds = new HashSet<Integer>();
    View v = parentLayout;
    while( v != null ) {
      seenIds.add( v.getId() );
      ViewParent vp = v.getParent();
      v = vp instanceof View ? (View)vp : null;
    }
    int id = 1;
    // magic number rumoured, from
    // http://stackoverflow.com/questions/6790623/programmatic-views-how-to-set-unique-ids.
    while( seenIds.contains(id) && id < 0x00FFFFFF-1 ) {
      id++;
    }
    return id;
  }
  
  public void destroy() {
    _relativeLayout.removeView( _tvRatingView );
    _tvRatingView.setVisibility( View.GONE );
    _tvRatingView = null;
   
    _relativeLayout.removeView( _videoView );
    _videoView.setVisibility( View.GONE );
    _videoView = null;
    
    _parentLayout.removeView( _relativeLayout );
    _relativeLayout.setVisibility( View.GONE );
    _relativeLayout = null;
  }
}
