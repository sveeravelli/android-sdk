package com.example.secureplayer.obsolete;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.MediaController;


/**
 * The documentation content provided in this file, including any code shown in it, is licensed under the Apache 2.0 license.
 * for more information see: <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>
 * 
 * </p>
 * 
 * Displays a video file.  The IVideoView interface includes methods for load images from various sources both encrypted and
 * non-encrypted (such as resources or content providers), takes care of computing its measurement from the video so that it
 * can be used in any layout manager, and provides various display options such as scaling and tinting.
 * 
 * For more info see <a href="http://developer.android.com/reference/android/widget/VideoView.html">Android VideoView</a>
 * 
 * @author Discretix
 */

public interface IDxVideoView {
	
	/**
	 * Checks if the prepared media playback can be paused. Call this function
	 * only after calling setVideoPath or setVideoUri
	 * 
	 * @return true if the video can be paused
	 */
	public abstract boolean canPause();

	/**
	 * Checks if the prepared media playback can be seeked backward. Call this
	 * function only after calling setVideoPath or setVideoUri
	 * 
	 * @return true if the video can be seeked backwards
	 */
	public abstract boolean canSeekBackward();

	/**
	 * Checks if the prepared media playback can be seeked forward. Call this
	 * function only after calling setVideoPath or setVideoUri
	 * 
	 * @return true if the video can be seeked forward
	 */
	public abstract boolean canSeekForward();

//	/**
//	 * Clears the value that was set with setDrmStreamingContextPath(). Call
//	 * this before switching the video sources
//	 */
//	public abstract void clearDrmStreamingContextPath();

	/**
	 * @return The buffer percentage
	 */
	public abstract int getBufferPercentage();

	/**
	 * @return The current playback position.
	 */
	public abstract int getCurrentPosition();

	/**
	 * @return The duration of the file.
	 */
	public abstract int getDuration();

	/**
	 * @return Checks whether the MediaPlayer is playing.
	 */
	public abstract boolean isPlaying();

	/**
	 * Default implementation of KeyEvent.Callback.onKeyDown(): perform press of
	 * the view when KEYCODE_DPAD_CENTER or KEYCODE_ENTER is released, if the
	 * view is enabled and clickable.
	 * 
	 * @param keyCode
	 *            A key code that represents the button pressed, from KeyEvent.
	 * @param event
	 *            The KeyEvent object that defines the button action
	 * @return If you handled the event, return true. If you want to allow the
	 *         event to be handled by the next receiver, return false.
	 */
	public abstract boolean onKeyDown(int keyCode, KeyEvent event);

	/**
	 * Implement this method to handle touch screen motion events.
	 * 
	 * @param ev
	 *            The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	public abstract boolean onTouchEvent(MotionEvent ev);

	/**
	 * Implement this method to handle trackball motion events. The relative
	 * movement of the trackball since the last event can be retrieve with
	 * MotionEvent.getX() and MotionEvent.getY(). These are normalized so that a
	 * movement of 1 corresponds to the user pressing one DPAD key (so they will
	 * often be fractional values, representing the more fine-grained movement
	 * information available from a trackball).
	 * 
	 * @param ev
	 *            The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	public abstract boolean onTrackballEvent(MotionEvent ev);

	/**
	 * Pauses playback.
	 */
	public abstract void pause();

	/**
	 * This function is provided only to maintain the compatibility with
	 * android.widget.VideoView
	 * 
	 * @param desiredSize
	 * @param measureSpec
	 * @return
	 */
	public abstract int resolveAdjustedSize(int desiredSize, int measureSpec);

	/**
	 * Seeks to specified time position.
	 * 
	 * If more than one seek is requested within 100 ms (if SDK uses the native
	 * player) or 50 ms (if SDK uses a 3rd party player), only the first and
	 * last seek requests will be executed. The last seek will occur 100 ms
	 * (native player) or 50 ms (3rd party player) after the first seek occurred.
	 * 
	 * @param msec
	 *            The time position to seek to
	 */
	public abstract void seekTo(int msec);

//	/**
//	 * Before streaming content use this function to request the media player to
//	 * save the content header in a writable location in the file system. The
//	 * content header can be used later on the get the rights info or to perform
//	 * other DRM oriented operations.
//	 * 
//	 * @param contextFilename
//	 *            A path in the file system where the application can write data
//	 */
//	public abstract void setDrmStreamingContextPath(String contextFilename);

	/**
	 * Sets a media controller that will control this DxVideoView instance
	 * 
	 * @param controller
	 *            An android.widget.MediaController object
	 */
	public abstract void setMediaController(MediaController controller);

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public abstract void setOnCompletionListener(OnCompletionListener l);

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public abstract void setOnErrorListener(OnErrorListener l);

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public abstract void setOnPreparedListener(MediaPlayer.OnPreparedListener l);

	/**
	 * Sets the video file to play
	 * 
	 * @param path
	 *            A path to a readable video file
	 * @throws IllegalArgumentException
	 * 			  If 'path' is not found
	 * @throws NullPointerException
	 * 			  If 'path' is null
	 */
	public abstract void setVideoPath(String path);
	
	/**
	 * Sets the video file to play
	 * Use this function only when implementing progressive download.
	 * 
	 * @param path
	 *            A path to a readable video file
	 * @param contentSize
	 * 			  The size of the file in bytes. Should used only when implementing progressive download.
	 * @throws IllegalArgumentException
	 * 			  If 'path' is not found
	 * @throws NullPointerException
	 * 			  If 'path' is null
	 */
	public abstract void setVideoPath(String path, int contentSize);

	/**
	 * Sets the video media to play.
	 * 
	 * @param uri
	 *            A URI to a readable video file
	 * @throws IllegalArgumentException
	 * 			  If 'uri' is invalid, e.g. includes illegal characters
	 * @throws NullPointerException
	 * 			  If 'uri' is null
	 */
	public abstract void setVideoURI(Uri uri);
	
	/**
	 * Sets the video media to play.
	 * This function must be used only when implementing progressive download.
	 * 
	 * @param uri
	 *            A URI to a readable video file
	 * @param contentSize
	 * 			 The size of the file in bytes. (It must be the exact file size.)
	 * @throws IllegalArgumentException
	 * 			  If 'uri' is invalid, e.g. includes illegal characters
	 * @throws NullPointerException
	 * 			  If 'uri' is null
	 */
	public abstract void setVideoURI(Uri uri, int contentSize);

	/**
	 * Starts or resumes playback.
	 */
	public abstract void start();

	/**
	 * Stops playback after playback has been stopped or paused.
	 */
	public abstract void stopPlayback();
	
	/**
	 * Call this from the activity's onResume event (mandatory)
	 */
	public abstract void onResume ();
	
	/**
	 * Call this from the activity's onPause event (mandatory)
	 */
	public abstract void onPause ();
	
	/**
	 * Call this from the activity's onDestroy event (mandatory)
	 */
	public abstract void onDestroy ();


}