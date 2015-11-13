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

package com.example.csp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

/**
 * Represents an asynchronous task. It has its own result class {@link CspResult} used to show a
 * dialog with some information on {@link AsyncTask#onPostExecute} method.
 */
public abstract class CspAsyncTaskBase extends AsyncTask<Void, Void, CspAsyncTaskBase.CspResult> {

    /**
     * Represents the task result.
     */
    protected static class CspResult {

        /** Message to show in the dialog message on onPostExecute. */
        protected final String mMsg;

        /** Determine if the task has successfully passed. */
        protected final Boolean mIsPass;

        /**
         * Constructor.
         * 
         * @param msg Message to show in the dialog message on onPostExecute.
         * @param isPass true if the task has successfully passed, otherwise false.
         */
        public CspResult(String msg, Boolean isPass) {
            this.mMsg = msg;
            this.mIsPass = isPass;
        }

        /**
         * Returns the message to show in the dialog message on onPostExecute.
         */
        public String getMsg() {
            return mMsg;
        }

        /**
         * Determines if the task has successfully passed.
         * 
         * @return true if the task has successfully passed, otherwise false.
         */
        public Boolean isPass() {
            return mIsPass;
        }

    }

    private ProgressDialog mProgressDialog;
    private final Activity mHostingActivity;
    private final String mTaskName;

    /**
     * Constructor.
     * 
     * @param hostingActivity Activity that holds the AsyncTask.
     * @param taskName Name for the task.
     */
    public CspAsyncTaskBase(Activity hostingActivity, String taskName) {
        super();
        mTaskName = taskName;
        mHostingActivity = hostingActivity;
    }

    @Override
    protected final void onPreExecute() {
        mProgressDialog = ProgressDialog.show(mHostingActivity, "", mTaskName + ". Please wait...",
                true);
    }

    @Override
    protected final void onPostExecute(CspResult result) {
        mProgressDialog.dismiss();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mHostingActivity);

        int icon = result.isPass() ? R.drawable.icon_pass : R.drawable.icon_fail;

        // set dialog message
        alertDialogBuilder.setTitle(mTaskName).setIcon(icon).setMessage(result.getMsg())
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mHostingActivity.finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
