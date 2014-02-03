package com.ooyala.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;

final public class TemporaryInternalStorageFile {

  private final File tmpFile;

  public TemporaryInternalStorageFile( final Context context, final String prefix, final String ext ) throws IOException {
    final File dir = context.getCacheDir();
    tmpFile = File.createTempFile( prefix, ext, dir );
    if( tmpFile != null && ! tmpFile.exists() ) {
      tmpFile.createNewFile();
    }
  }

  public String getAbsolutePath() {
    return tmpFile == null ? "" : tmpFile.getAbsolutePath();
  }

  public void write( final String body ) throws FileNotFoundException {
    if( tmpFile != null ) {
      final PrintWriter pw = new PrintWriter( tmpFile );
      pw.write( body );
      pw.flush();
      pw.close();
    }
  }
}