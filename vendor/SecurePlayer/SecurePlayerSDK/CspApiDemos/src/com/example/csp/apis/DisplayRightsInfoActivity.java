/*******************************************************************************
 * Copyright
 *  This code is strictly confidential and the receiver is obliged to use it
 *  exclusively for his or her own purposes. No part of Viaccess Orca code may be
 *  reproduced or transmitted in any form or by any means, electronic or
 *  mechanical, including photocopying, recording, or by any information storage
 *  and retrieval system, without permission in writing from Viaccess Orca.
 *  The information in this code is subject to change without notice. Viaccess Orca
 *  does not warrant that this code is error free. If you find any problems
 *  with this code or wish to make comments, please report them to Viaccess Orca.
 *  
 *  Trademarks
 *  Viaccess Orca is a registered trademark of Viaccess S.A in France and/or other
 *  countries. All other product and company names mentioned herein are the
 *  trademarks of their respective owners.
 *  Viaccess S.A may hold patents, patent applications, trademarks, copyrights
 *  or other intellectual property rights over the code hereafter. Unless
 *  expressly specified otherwise in a Viaccess Orca written license agreement, the
 *  delivery of this code does not imply the concession of any license over
 *  these patents, trademarks, copyrights or other intellectual property.
 *******************************************************************************/

package com.example.csp.apis;

import android.app.Activity;
import android.os.Bundle;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.IDxRightsInfo;
import com.discretix.drmdlc.api.exceptions.DrmAndroidPermissionMissingException;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmCommunicationFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmNotProtectedException;
import com.example.csp.CspAsyncTaskBase;
import com.example.csp.CspConstants;
import com.example.csp.RightsInfoLocalizer;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Represents the activity to display content Rights information.
 */
public class DisplayRightsInfoActivity extends Activity {

    /**
     * Represents the asynchronous task to retrieve content Rights.
     */
    private class DrmExecuter extends CspAsyncTaskBase {
        
        public DrmExecuter() {
            super(DisplayRightsInfoActivity.this, "Rights Info");
        }

        @Override
        protected CspResult doInBackground(Void... arg0) {
            String localContentFilePath = CspConstants.getActiveContent().getTemplocalFile();
            String userMessage;
            Boolean doHaveRights = false;
            try {

                // Get the CSP singleton instance
                IDxDrmDlc cspApiSingleton = DxDrmDlc.getDxDrmDlc(DisplayRightsInfoActivity.this);
                IDxRightsInfo[] rights = cspApiSingleton.getRightsInfo(DisplayRightsInfoActivity.this,
                        localContentFilePath);
                userMessage = "Rights info: "
                        + RightsInfoLocalizer.toSringRightsInfo(DisplayRightsInfoActivity.this,
                                rights);
                doHaveRights = cspApiSingleton.verifyRights(localContentFilePath);
            } catch (DrmClientInitFailureException e) {
                userMessage = "Exception: DrmClientInitFailureException";
            } catch (DrmGeneralFailureException e) {
                userMessage = "Exception: DrmGeneralFailureException";
            } catch (FileNotFoundException e) {
                userMessage = "Exception: FileNotFoundException";
            } catch (DrmNotProtectedException e) {
                userMessage = "Exception: DrmNotProtectedException";
            } catch (DrmInvalidFormatException e) {
                userMessage = "Exception: DrmInvalidFormatException";
            } catch (DrmCommunicationFailureException e) {
                userMessage = "Exception: DrmCommunicationFailureException";
            } catch (IOException e) {
                userMessage = "Exception: IOException";
            } catch (DrmAndroidPermissionMissingException e) {
            	String[] list = e.getMissingPermissions();
            	userMessage = "Exception: DrmAndroidPermissionMissingException ";
            	for (String permission : list)
            	{
            		userMessage+=permission + " ";
            	}
			}

            return new CspResult(userMessage, doHaveRights);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DrmExecuter().execute();
    }
}
