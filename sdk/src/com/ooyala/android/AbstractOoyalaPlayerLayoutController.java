package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

public abstract class AbstractOoyalaPlayerLayoutController implements LayoutController {
	public static enum DefaultControlStyle {
		NONE, AUTO
	};

	protected OoyalaPlayerLayout _layout = null;
	protected Dialog _fullscreenDialog = null;
	protected OoyalaPlayerLayout _fullscreenLayout = null;
	protected OoyalaPlayerControls _inlineControls = null;
	protected OoyalaPlayerControls _fullscreenControls = null;
	protected OoyalaPlayerControls _inlineOverlay = null;
	protected OoyalaPlayerControls _fullscreenOverlay = null;
	protected OoyalaPlayer _player = null;
	protected boolean _fullscreenButtonShowing = true;
	protected List<String> optionList;

	/**
	 * Instantiate an AbstractOoyalaPlayerLayoutController
	 *
	 * @param l the layout to use
	 * @param pcode the provider code to use
	 * @param domain the embed domain to use
	 */
	public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String pcode, String domain) {
		this(l, pcode, domain, DefaultControlStyle.AUTO);
	}

	/**
	 * Instantiate an AbstractOoyalaPlayerLayoutController
	 *
	 * @param l the layout to use
	 * @param pcode the provider code to use
	 * @param domain the embed domain to use
	 * @param generator An embedTokenGenerator used to sign SAS requests
	 */
	public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String pcode, String domain, EmbedTokenGenerator generator) {
		this(l, pcode, domain, DefaultControlStyle.AUTO, generator);
	}

	/**
	 * Instantiate an AbstractOoyalaPlayerLayoutController
	 *
	 * @param l the layout to use
	 * @param p the instantiated player to use
	 */
	public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p) {
		this(l, p, DefaultControlStyle.AUTO);
	}

	/**
	 * Instantiate an AbstractOoyalaPlayerLayoutController
	 *
	 * @param l the layout to use
	 * @param pcode the provider code to use
	 * @param domain the embed domain to use
	 * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
	 */
	public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String pcode, String domain, DefaultControlStyle dcs) {
		this(l, new OoyalaPlayer(pcode, domain), dcs);
	}

	/**
	 * Instantiate an AbstractOoyalaPlayerLayoutController
	 *
	 * @param l the layout to use
	 * @param pcode the provider code to use
	 * @param domain the embed domain to use
	 * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
	 * @param generator An embedTokenGenerator used to sign SAS requests
	 */
	public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String pcode, String domain,
			DefaultControlStyle dcs, EmbedTokenGenerator generator) {
		this(l, new OoyalaPlayer(pcode, domain, generator), dcs);
	}

	/**
	 * Instantiate an AbstractOoyalaPlayerLayoutController
	 *
	 * @param l the layout to use
	 * @param p the instantiated player to use
	 * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
	 */
	public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p, DefaultControlStyle dcs) {
		_player = p;
		_layout = l;
		_player.setLayoutController(this);
		_layout.setLayoutController(this);
		if (dcs == DefaultControlStyle.AUTO) {
			setInlineControls(createDefaultControls(_layout, false));
			_inlineControls.hide();
			_player.addObserver(_inlineControls);
		}
	}

	public void setInlineOverlay(OoyalaPlayerControls controlsOverlay) {
		_inlineOverlay = controlsOverlay;
		_inlineOverlay.setOoyalaPlayer(_player);
	}

	public void setFullscreenOverlay(OoyalaPlayerControls controlsOverlay) {
		_fullscreenOverlay = controlsOverlay;
		_fullscreenOverlay.setOoyalaPlayer(_player);
	}

	/**
	 * Get the OoyalaPlayer associated with this Controller
	 *
	 * @return the OoyalaPlayer
	 */
	public OoyalaPlayer getPlayer() {
		return _player;
	}

	/**
	 * Get the current active layout
	 *
	 * @return the current active layout
	 */
	@Override
	public FrameLayout getLayout() {
		return isFullscreen() ? _fullscreenLayout.getPlayerFrame() : _layout.getPlayerFrame();
	}

	public void setInlineControls(OoyalaPlayerControls controls) {
		_inlineControls = controls;
		_inlineControls.setFullscreenButtonShowing(_fullscreenButtonShowing);
	}

	public void setFullscreenControls(OoyalaPlayerControls controls) {
		_fullscreenControls = controls;
		_fullscreenControls.setFullscreenButtonShowing(_fullscreenButtonShowing);
	}

	public OoyalaPlayerControls getControls() {
		return isFullscreen() ? _fullscreenControls : _inlineControls;
	}

	public OoyalaPlayerControls getOverlay() {
		return isFullscreen() ? _fullscreenOverlay : _inlineOverlay;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, OoyalaPlayerLayout source) {
		// the MediaController will hide after 3 seconds - tap the screen to make it appear again
		if (_player != null) {
			switch (_player.getState()) {
			case INIT:
			case LOADING:
			case ERROR:
				return false;
			default:
				if (getControls() != null) {
					if (getControls().isShowing()) {
						getControls().hide();
					} else {
						getControls().show();
					}
				}
				if (getOverlay() != null) {
					if (getOverlay().isShowing()) {
						getOverlay().hide();
					} else {
						getOverlay().show();
					}
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public void setFullscreen(boolean fullscreen) {}

	@Override
	public boolean isFullscreen() {
		return false;
	}

	public OoyalaPlayerControls createDefaultControls(OoyalaPlayerLayout layout, boolean fullscreen) {
		if (fullscreen) {
			return new DefaultOoyalaPlayerFullscreenControls(_player, layout);
		} else {
			return new DefaultOoyalaPlayerInlineControls(_player, layout);
		}
	}

	/**
	 * Create and display the list of available languages.
	 */
	@Override
	public void showClosedCaptionsMenu() {
		AlertDialog dialog;
		Set<String> languageSet = _player.getAvailableClosedCaptionsLanguages();
		languageSet.add("None");

		final String[] items = languageSet.toArray(new String[0]);
		final Context context = _layout.getContext();

		if (this.optionList == null) {
			this.optionList = new ArrayList<String>();
			this.optionList.add("Languages");
			this.optionList.addAll(languageSet);
			this.optionList.add("Presentation Styles");
			this.optionList.add("Roll-Up");
			this.optionList.add("Paint-On");
			this.optionList.add("Pop-On");
		}
		ClosedCaptionArrayAdapter optionAdapter = new ClosedCaptionArrayAdapter(context, android.R.layout.simple_list_item_1, this.optionList);

		final ListView listView = new ListView(context);
		listView.setAdapter(optionAdapter);

		// Listen for click events
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
				if (position <= items.length) {
					String language = !items[position - 1].equals("None") ? items[position - 1] : null;
					_player.setClosedCaptionsLanguage(language);
					// Mark the item
				} else {
					// Change the presentation
					//_player.setClosedCaptionsStyle();
				}
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(listView);
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * setFullscreenButtonShowing will enable and disable visibility of the fullscreen button
	 */
	public void setFullscreenButtonShowing(boolean showing){

		if (_inlineControls != null) {
			_inlineControls.setFullscreenButtonShowing(showing);
		}
		if (_fullscreenControls != null) {
			_fullscreenControls.setFullscreenButtonShowing(showing);
		}
		_fullscreenButtonShowing = showing;
	}

	class ClosedCaptionArrayAdapter extends ArrayAdapter<String> {

		private final List<String> itemList;

		public ClosedCaptionArrayAdapter(Context context,
				int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			this.itemList = objects;
		}

		@Override
		public boolean isEnabled(int position) {
			return (position != 0) && (position != this.itemList.indexOf("Presentation Styles"));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			if (position == 0 || position == this.itemList.indexOf("Presentation Styles")) {
				v.setBackgroundColor(Color.LTGRAY);
			}
			return v;
		}

	}
}