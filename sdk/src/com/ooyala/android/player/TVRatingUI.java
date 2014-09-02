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

  private static final int MARGIN_DIP = 5;
  private RelativeLayout _relativeLayout;
  private MovieView _movieView;
  private FCCTVRatingView _tvRatingView;
  
  public RelativeLayout getRelativeLayout() {
    return _relativeLayout;
  }

  public MovieView getMovieView() {
    return _movieView;
  }

  public FCCTVRatingView getTVRatingView() {
    return _tvRatingView;
  }

  public TVRatingUI( View videoView, ViewGroup parentLayout, TVRatingConfiguration tvRatingConfiguration ) {
    Context context = parentLayout.getContext();
    /*
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/movie_layout"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:background="#00000000" >
     */
    _relativeLayout = new RelativeLayout( context );
    _relativeLayout.setBackgroundColor( android.graphics.Color.TRANSPARENT );
    FrameLayout.LayoutParams paramsForRelative = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT );
    parentLayout.addView( _relativeLayout, paramsForRelative );
    /*
        <com.ooyala.android.player.MovieView
            android:id="@+id/movie_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:background="#FF000000" />
     */
    if( videoView.getId() == View.NO_ID ) {
      videoView.setId( getUnusedId( parentLayout ) );
    }
    videoView.setBackgroundColor( android.graphics.Color.BLACK );
    RelativeLayout.LayoutParams paramsForMovieView = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
    paramsForMovieView.addRule( RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE );
    _relativeLayout.addView( videoView, paramsForMovieView );
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
    _tvRatingView = new FCCTVRatingView( context );
    _tvRatingView.setVisibility( View.INVISIBLE );
    _tvRatingView.setBackgroundColor( android.graphics.Color.TRANSPARENT );
    RelativeLayout.LayoutParams paramsForRatingsView = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_TOP, videoView.getId() );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_LEFT, videoView.getId() );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_BOTTOM, videoView.getId() );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_RIGHT, videoView.getId() );
    int margin = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, MARGIN_DIP, context.getResources().getDisplayMetrics() );
    paramsForRatingsView.setMargins( margin, margin, margin, margin ); 
    _relativeLayout.addView( _tvRatingView, paramsForRatingsView );
    _tvRatingView.setTVRatingConfiguration( tvRatingConfiguration );
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
}
