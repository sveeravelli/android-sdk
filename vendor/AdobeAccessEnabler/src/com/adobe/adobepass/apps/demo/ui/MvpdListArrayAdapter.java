/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.adobe.adobepass.apps.demo.R;

import java.util.List;

public class MvpdListArrayAdapter extends ArrayAdapter<MvpdListItem> {
	private int resource;

	public MvpdListArrayAdapter(Context context, int textViewResourceId, List<MvpdListItem> objects) {
		super(context, textViewResourceId, objects);
		
		resource = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout mvpdItemView;

		MvpdListItem mvpdListItem = getItem(position);
		
		if (convertView == null) {
			mvpdItemView = new LinearLayout(getContext());
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(resource, mvpdItemView, true);
		} else {
			mvpdItemView = (LinearLayout) convertView;
		}

		TextView mvpdNameTv = (TextView) mvpdItemView.findViewById(R.id.mvpd_name);
		mvpdNameTv.setText(mvpdListItem.getMvpd().getDisplayName());

		return mvpdItemView;
	}	

}
