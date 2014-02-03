package com.ooyala.android;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;
import android.util.Log;

final public class TemporaryInternalStorageFileManager {
  private static final String TAG = "TemporaryInternalStorageFiles";
  private static final String PRE_PRE_FIX = "OOTISF_";
  private static final long TMP_LIFESPAN_MSEC = 5 * 60 * 1000; // 5 minutes.
  private static AtomicLong s_nextTmpId = new AtomicLong();

  public TemporaryInternalStorageFile next( final Context context, final String prefix, final String ext ) throws IOException {
    cleanup( context );
    final long id = s_nextTmpId.getAndIncrement();
    return new TemporaryInternalStorageFile( context, PRE_PRE_FIX + s_nextTmpId.get() + "_" + prefix, ext );
  }

  public void cleanup( final Context context ) {
    final File dir = context.getCacheDir();
    Log.d( TAG, "cleanup(): dir=" + dir );
    if( dir != null && dir.isDirectory() ) {
      final long now = new Date().getTime();
      for( final File f : dir.listFiles( new FileFilter() {
        @Override
        public boolean accept( final File f ) {
          final boolean isFile = f.isFile();
          final boolean nameMatches = f.getName().startsWith( PRE_PRE_FIX );
          final boolean isOld = now - f.lastModified() >= TMP_LIFESPAN_MSEC;
          Log.d( TAG, "cleanup(): f=" + f.getAbsolutePath() + ", isFile=" + isFile + ", nameMatches=" + nameMatches + ", isOld=" + isOld );
          return isFile && nameMatches && isOld;
        }
      } ) ) {
        Log.d( TAG, "cleanup(): deleting f=" + f.getAbsolutePath() + ", name=" + f.getName() );
        f.delete(); // in Android Java, File.delete() doesn't throw exceptions.
      }
    }
  }
}
