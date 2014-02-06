package com.example.secureplayer;



public class DxContentItem {
	
	public enum ECustomDataType{
		CUSTOM_DATA_IS_TEXT { 
			@Override
			public String toString() {
				return "TEXT";
			}
		},
		CUSTOM_DATA_IS_URL{ 
			@Override
			public String toString() {
				return "URL";
			}
		},
		CUSTOM_DATA_IS_FILE{
			@Override
			public String toString() {
				return "FILE";
			}
		}
	}
	
	private String mName;
	private String mContentUrl;
	private String mInitiatorUrl;
	private Boolean mIsStreaming;
	private String mCustomData;
	private String mCustomUrl;
	private ECustomDataType mCustomDataType;
	private String mFileName;
	private String[] mCookies;	
	
	/**
	 * {@link DxContentItem} builder
	 *
	 */
	public static class Builder{
		private String mName                    = null;
		private String mContentUrl              = null;
		private String mInitiatorUrl            = null;
		private Boolean mIsStreaming            = true;
		private String mCustomData              = null;
		private String mCustomUrl               = null;
		private ECustomDataType mCustomDataType = ECustomDataType.CUSTOM_DATA_IS_TEXT;
		private String[] mCookies				= null;
		
		/**
		 * Create {@link DxContentItem} builder instance
		 * @param name content name
		 */
		public Builder(String name){
			mName = name;
		}
		
		public Builder setContentUrl(String url){
			mContentUrl = url;
			return this;
		}
		public Builder setIsStreaming(Boolean state){
			mIsStreaming = state;
			return this;
		}
		
		public Builder setInitiatorUrl(String url){
			mInitiatorUrl = url;
			return this;
		}
		public Builder setCustomData(String customData,
										   ECustomDataType customDataType){
			mCustomData     = customData;
			mCustomDataType = customDataType;
			return this;
		}
		public Builder setCustomUrl(String url){
			mCustomUrl = url;
			return this;
		}
		public Builder setCookies(String[] cookies){
			mCookies = cookies;
			return this;
		}
		public DxContentItem build(){
			return new DxContentItem(this);
		}
	}	
	
	private DxContentItem(Builder builder){
		mName           = builder.mName;
		mContentUrl     = builder.mContentUrl;
		mInitiatorUrl   = builder.mInitiatorUrl;
		mIsStreaming    = builder.mIsStreaming;
		mCustomData     = builder.mCustomData;
		mCustomDataType = builder.mCustomDataType;
		mCustomUrl      = builder.mCustomUrl;
		mCookies        = builder.mCookies;
	}
	
	public String getCustomData() {
		return mCustomData;
	}

	public void setCustomData(String customData) {
		this.mCustomData = customData;
	}

	public String getCustomUrl() {
		return mCustomUrl;
	}

	public void setCustomUrl(String customUrl) {
		this.mCustomUrl = customUrl;
	}

	public ECustomDataType getCustomDataType() {
		return mCustomDataType;
	}

	public void setCustomDataType(ECustomDataType customDataType) {
		this.mCustomDataType = customDataType;
	}
	
	public String[] getCookiesArry() {
		return mCookies;
	}

	public void setmFileName(String mFileName) {
		this.mFileName = mFileName;
	}

	public String getName() {
		return mName;
	}
	public String getContentUrl() {
		return mContentUrl;
	}
	public String getInitiatorUrl() {
		return mInitiatorUrl;
	}
	
	
	public static String generateFileName(String url){
		String lastComponent = url.substring(url.lastIndexOf('/') + 1);
		
		if (lastComponent.lastIndexOf('?')>0){//check if there is a query string
			lastComponent = lastComponent.substring(0, lastComponent.lastIndexOf('?'));//skip query string
		}
		if (lastComponent.lastIndexOf('.')>0){
			return String.format("%s/%08X%s", DxConstants.CONTENT_DIR,
					                          url.hashCode(),
					                          lastComponent.substring(lastComponent.lastIndexOf('.')));
		}
		return String.format("%s/%08X_%s", DxConstants.CONTENT_DIR,
				                           url.hashCode(),
				                           lastComponent);
	}
	
	public String getTemplocalFile() {
		
		if (null == mFileName){
			return generateFileName(mContentUrl);
		}
		
		return mFileName;
		
	}
	
	public String getPlayBackPath() {
		if (mIsStreaming == true) {
			return mContentUrl;
		}else {
			return getTemplocalFile();
		}
	}

	public void setName(String name) {
		mName = name;
	}

	public void setContentUrl(String contentUrl) {
		mContentUrl = contentUrl;
	}
	public void setInitiatorUrl(String initiatorUrl) {
		mInitiatorUrl = initiatorUrl;
	}
	public void setmIsStreaming(Boolean mIsStreaming) {
		this.mIsStreaming = mIsStreaming;
	}
	
	public boolean IsStreaming() {
		return mIsStreaming;
	}

	public void setCookiesFromStr(String cookies) {
		if (cookies != null && cookies.length() > 0)
		{
			mCookies = cookies.split(";");
			for (int i = 0; i < mCookies.length; i++) {
				mCookies[i] = mCookies[i].trim();
			}
		}else {
			mCookies = null;
		}
	}

	public String getCookiesStr() {
		StringBuffer cookiesStr = new StringBuffer();
		if (mCookies != null && mCookies.length > 0)
		{
			for (int i = 0; i < mCookies.length; i++) {
				cookiesStr.append(mCookies[i]);
			}
			return cookiesStr.toString();
		}
		return "";
	}
	
}
