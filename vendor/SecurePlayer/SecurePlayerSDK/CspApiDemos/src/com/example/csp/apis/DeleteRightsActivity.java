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
import com.discretix.drmdlc.api.exceptions.DrmAndroidPermissionMissingException;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmNotProtectedException;
import com.example.csp.CspAsyncTaskBase;
import com.example.csp.CspConstants;
import com.example.csp.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Represents the activity to delete Rights related to a content.
 * 
 * @see {@link AcquireRightsActivity} and {@link InitiatorActivity}.
 */
public class DeleteRightsActivity extends Activity {

    /**
     * Represents the asynchronous task to delete content Rights.
     */
    private class DrmExecuter extends CspAsyncTaskBase {

        public DrmExecuter() {
            super(DeleteRightsActivity.this, "Delete Rights");
        }

        @Override
        protected CspResult doInBackground(Void... arg0) {
            String userMessage;
            String fileName = CspConstants.getActiveContent().getTemplocalFile();
            Boolean isPassed = false;
            try {
                // First we need check if there is a local Content Path.
                if (!Utils.checkFileExists(fileName)) {
                    userMessage = "You need to download the file before you delete Rights.";
                    return new CspResult(userMessage, false);
                }

                // Get the CSP singleton instance
                IDxDrmDlc cspApiSingleton = DxDrmDlc.getDxDrmDlc(DeleteRightsActivity.this, null);

                cspApiSingleton.deleteRights(fileName);

                userMessage = "Delete license successful";
                isPassed = true;
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
            } catch (IOException e) {
                userMessage = "Exception: IOException, download failed";
            } catch (DrmAndroidPermissionMissingException e) {
            	String[] list = e.getMissingPermissions();
            	userMessage = "Exception: DrmAndroidPermissionMissingException ";
            	for (String permission : list)
            	{
            		userMessage+=permission + " ";
            	}
            	
			}

            return new CspResult(userMessage, isPassed);
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DrmExecuter().execute();
    }

}
