package com.ooyala.android;

public class OOMediaPlayerError {
    
	private String code;
	private String messageKey;
	private String titleKey;
	private String category;
	
	public OOMediaPlayerError(String code, String messageKey, String titleKey, String category) {
		this.code = code;
		this.messageKey = messageKey;
		this.titleKey = titleKey;
		this.category = category;
	}
	
	public OOMediaPlayerError(String code, String messageKey, String titleKey) {
		this(code, messageKey, titleKey, null);
	}
	
	public OOMediaPlayerError(String code, String messageKey) {
		this(code, messageKey, null, null);
	}
	
}
