package com.ooyala.android.visualon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlcDebug;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmServerSoapErrorException;
import com.discretix.vodx.VODXPlayerImpl;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.item.Stream;
import com.visualon.OSMPPlayer.VOCommonPlayer;

/**
 * Static methods that are used to perform DRM related activities in the VisualOn Stream Player.
 * The class was created to abstract the VisualOnStreamPlayer from all DRM code, to make it compilable
 * without errors when DRM is disabled
 *
 */
class DiscredixDrmUtils {
  private static final String TAG = DiscredixDrmUtils.class.getName();
  private static final String SECURE_PLAYER_VERSION = "03_00_05_0962";

  public static void enableDebugging(Context context, boolean extreme) {
    try {
      DxLogConfig config;
      if( extreme ) {
        config = new DxLogConfig(
          DxLogConfig.LogLevel.Verbose,
          0,
          new File( context.getExternalCacheDir(), "vo_dx.log" ).toString(),
          true
        );
      }
      else {
        config = new DxLogConfig(
          DxLogConfig.LogLevel.Verbose,
          0
        );
      }
      final IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(context, config);
    } catch( DrmClientInitFailureException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Checks if the device has been personalized
   * @return true if personalized, false if not
   */
  public static boolean isDevicePersonalized(Context context) {
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(context, config);
      return dlc.personalizationVerify();
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Checks the given local file path if file is DRM protected
   * @param streamUrl
   * @return true if the stream is protected with DRM, false otherwise
   */
  public static boolean isStreamProtected(Context context, String localFilePath) {
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    boolean isDrmContent = false;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(context, config);
      DebugMode.logD(TAG, "isStreamProtected. Discredix Version: " + dlc.getDrmVersion());

      isDrmContent = dlc.isDrmContent(localFilePath);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
    }
    return isDrmContent;
  }

  /**
   * Compares the loaded Discredix library's version, and compares it with expected value
   * @param context
   * @return true if version is expected value, false otherwise
   */
  public static boolean isDiscredixVersionCorrect(Context context) {
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(context, config);
      DebugMode.logD(TAG, "isDiscredixVersionCorrect. Discredix Version: " + dlc.getDrmVersion());

      final String runningVersion = dlc.getDrmVersion();
      final boolean foundExpectedVersion = runningVersion.contains( SECURE_PLAYER_VERSION );
      if (!foundExpectedVersion) {
        DebugMode.logE(TAG, "Discredix Version was not expected! Looking for: " + SECURE_PLAYER_VERSION + ", Actual: " + dlc.getDrmVersion());
        DebugMode.logE(TAG, "Please ask your CSM for updated versions of the Discredix/SecurePlayer Libraries");
          return false;
      }
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
      return false;
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  /**
   * Checks if the file is DRM enabled, and if it is, if the file's DRM rights are valid.
   * @param localFilename file path to locally downloaded file
   * @return true if file can now be played, false otherwise.
   */
  public static boolean canFileBePlayed(Context context, Stream stream, String localFilename) {
    if (!Stream.DELIVERY_TYPE_SMOOTH.equals(stream.getDeliveryType())) return true;
    if (localFilename == null) return false;
    if (!isStreamProtected(context, localFilename)) return true;

    DxLogConfig config = null;
    IDxDrmDlc dlc;
    boolean areRightsVerified = false;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(context, config);

      areRightsVerified = dlc.verifyRights(localFilename);

    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
    } catch (DrmInvalidFormatException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    return areRightsVerified;
  }

  /**
   * Parse SOAP from Playready license server to see if there is a known Playready error
   * @param exception The exception that is passed from the Playready license server
   * @return An OoyalaException with the intended error for the application
   */
  public static OoyalaException handleDRMError(Exception exception) {
    OoyalaException error = null;

    // If this is not a SOAP error, just bubble it up as a general failure
    if(exception.getClass() != DrmServerSoapErrorException.class) {
      DebugMode.logE(TAG, "Error with VisualOn Acquire Rights code");
      error = new OoyalaException(OoyalaErrorCode.ERROR_DRM_GENERAL_FAILURE, exception);
    }
    else {
      String description =  ((DrmServerSoapErrorException)exception).getCustomData().replaceAll("<[^>]+>", "");

      if ("invalid token".equals(description)) {
        DebugMode.logE(TAG, "VisualOn Rights error: Invalid token");
        error = new OoyalaException(OoyalaErrorCode.ERROR_DEVICE_INVALID_AUTH_TOKEN);
      }
      else if ("device limit reached".equals(description)) {
        DebugMode.logE(TAG, "VisualOn Rights error: Device limit reached");
        error = new OoyalaException(OoyalaErrorCode.ERROR_DEVICE_LIMIT_REACHED);
      }
      else if ("device binding failed".equals(description)) {
        DebugMode.logE(TAG, "VisualOn Rights error: Device binding failed");
        error = new OoyalaException(OoyalaErrorCode.ERROR_DEVICE_BINDING_FAILED);
      }
      else if ("device id too long".equals(description)) {
        DebugMode.logE(TAG, "VisualOn Rights error: Device ID too long");
        error = new OoyalaException(OoyalaErrorCode.ERROR_DEVICE_ID_TOO_LONG);
      }
      else {
        DebugMode.logE(TAG, "General SOAP error from DRM server: " + description);
        error = new OoyalaException(OoyalaErrorCode.ERROR_DRM_RIGHTS_SERVER_ERROR, description);
      }
    }
    return error;
  }

  /**
   * Do a simple initialization of the DRM client.  This is to deal with some bugs that seemingly pop up when
   * initializing for the first time.
   * @param context
   */
  public static void warmDxDrmDlc(Context context){
    DebugMode.logD(TAG, "Warming DxDrmDlc");
    try {
      IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(context, null);
      try {
        DebugMode.logD(TAG, "Discredix Version: " + dlc.getDrmVersion());

      } catch (DrmGeneralFailureException e) {
        DebugMode.logE(TAG, "Failed trying to get discredix version");
        e.printStackTrace();
      }
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
    }
  }

  /**
   * Generate a VODXPlayerImpl from Discredix
   * This is so VisualOnStreamPlayer can have no references to Discredix code
   * @return
   */
  public static VOCommonPlayer getVODXPlayerImpl() {
    return new VODXPlayerImpl();
  }
}
