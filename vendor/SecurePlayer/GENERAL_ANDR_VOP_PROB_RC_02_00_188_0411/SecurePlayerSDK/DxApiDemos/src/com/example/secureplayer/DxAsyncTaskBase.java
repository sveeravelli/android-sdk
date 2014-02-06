package com.example.secureplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

public abstract class DxAsyncTaskBase extends AsyncTask<Void, Void, DxAsyncTaskBase.DxResult> {
	
	protected static class DxResult{
		protected final String mMsg;
		protected final Boolean mIsPass;
		public DxResult(String msg, Boolean isPass) {
			this.mMsg = msg;
			this.mIsPass = isPass;
		}
		public String getMsg() {
			return mMsg;
		}
		public Boolean isPass() {
			return mIsPass;
		}
		
	}
	
	
	
	
	private ProgressDialog mProgressDialog;
	private final Activity mHostingActivity;
	private final String mTaskName;
	
	
	public DxAsyncTaskBase(Activity hostingActivity, String taskName) {
		super();
		mTaskName = taskName;
		mHostingActivity = hostingActivity;
	}

	@Override
	protected final void onPreExecute() {
		mProgressDialog = ProgressDialog.show(mHostingActivity, "",
				mTaskName + ". Please wait...", true);
	}

	@Override
	protected final void onPostExecute(DxResult result) {
		mProgressDialog.dismiss();
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mHostingActivity);
		
		int icon = result.isPass() ? R.drawable.icon_pass : R.drawable.icon_fail;

		// set dialog message
		alertDialogBuilder
		    .setTitle(mTaskName)
		    .setIcon(icon)
			.setMessage(result.getMsg())
			.setCancelable(false)
			.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					mHostingActivity.finish();
				}
			  });

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
	}
	
}
