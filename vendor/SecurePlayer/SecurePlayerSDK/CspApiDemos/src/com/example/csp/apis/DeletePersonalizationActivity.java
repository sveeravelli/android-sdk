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
import com.example.csp.CspAsyncTaskBase;

/**
 * Represents the activity to delete Personalization.
 * 
 * @see {@link PersonalizationActivity}.
 */
public class DeletePersonalizationActivity extends Activity {

    /**
     * Represents the asynchronous task to delete Personalization.
     */
    private class PersonalizationExecuter extends CspAsyncTaskBase {

        public PersonalizationExecuter() {
            super(DeletePersonalizationActivity.this, "Deleting Personalization");
        }

        @Override
        protected CspResult doInBackground(Void... params) {
            // Will be overwritten on any error
            String message = "Personalization was deleted";
            Boolean isPassed = false;

            try {
                // Get the CSP singleton instance
                IDxDrmDlc cspApiSingleton = DxDrmDlc.getDxDrmDlc(DeletePersonalizationActivity.this);
                if (!cspApiSingleton.personalizationVerify()) {
                    message = "the device is not personalized";
                } else {
                    cspApiSingleton.getDebugInterface().deletePersonalization();
                    cspApiSingleton.getDebugInterface().deletePlayReadyStore();
                }
                isPassed = true;
            } catch (DrmClientInitFailureException e1) {
                message = "Exception: DrmClientInitFailureException";
            } catch (DrmGeneralFailureException e) {
                message = "Exception: DrmGeneralFailureException";
            } catch (DrmAndroidPermissionMissingException e) {
            	String[] list = e.getMissingPermissions();
            	message = "Exception: DrmAndroidPermissionMissingException ";
            	for (String permission : list)
            	{
            		message+=permission + " ";
            	}
            	
			}
            return new CspResult(message, isPassed);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PersonalizationExecuter().execute();
    }
}
