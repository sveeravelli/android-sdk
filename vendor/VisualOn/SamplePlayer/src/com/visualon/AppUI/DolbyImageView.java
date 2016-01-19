package com.visualon.AppUI;


import com.visualon.AppPlayerCommonFeatures.CPlayer;

import android.content.Context;

import android.widget.ImageView;

public class DolbyImageView extends ImageView{
	
    private boolean mViewEnable = false;
	
    public DolbyImageView(Context context, CPlayer player) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	public boolean isViewEnabled() {
		return mViewEnable;
	}

}
