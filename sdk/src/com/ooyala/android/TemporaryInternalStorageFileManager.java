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
    return new TemporaryInternalStorageFile( context, PRE_PRE_FIX + s_nextTmpId + "_" + prefix, ext );
  }
  
  public void cleanup( final Context context ) {
    final long now = new Date().getTime();
    final File dir = context.getCacheDir();
    Log.d( TAG, "cleanup(): now=" + now + ", dir=" + dir );
    if( dir != null && dir.isDirectory() ) {
      for( File f : dir.listFiles( new FileFilter() {
        @Override
        public boolean accept( final File file ) {
          return file.isFile();
        }
      } ) ) {
        Log.d( TAG, "cleanup(): f=" + f.getAbsolutePath() + ", name=" + f.getName() );
        if( f.getName().startsWith( PRE_PRE_FIX ) ) {
          long diff = now - f.lastModified();
          Log.d( TAG, "cleanup(): f=" + f.getAbsolutePath() + ", " + diff + " >? " + TMP_LIFESPAN_MSEC );
          if( diff > TMP_LIFESPAN_MSEC ) {
            Log.d( TAG, "cleanup(): deleting " + f.getAbsolutePath() );
            f.delete();
          }
        }
      }
    }
  }
  
  static final public class TemporaryInternalStorageFile {
    private static final String TAG = "TemporaryInternalStorageFile";
    private File tmpBootHmtlFile;  
  
    private TemporaryInternalStorageFile( final Context context, final String prefix, final String ext ) throws IOException {
      final File dir = context.getCacheDir();
      tmpBootHmtlFile = File.createTempFile( prefix + "_" + s_nextTmpId, ext, dir );
      if( ! tmpBootHmtlFile.exists() ) {
        tmpBootHmtlFile.createNewFile();
      }    
    } 

    public String getAbsolutePath() {
      return tmpBootHmtlFile == null ? "" : tmpBootHmtlFile.getAbsolutePath();
    }

    public void write( final String body ) throws FileNotFoundException { 
      if( tmpBootHmtlFile != null ) {
        final PrintWriter pw = new PrintWriter( tmpBootHmtlFile );
        pw.write( body );
        pw.flush();
        pw.close();
      }
    }
  }
}
