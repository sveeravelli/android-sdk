/************************************************************************
VisualOn Proprietary
Copyright (c) 2014, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.AppUI;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.visualon.AppBehavior.AppBehaviorManagerImpl.OPTION_ID;
import com.visualon.AppPlayerCommonFeatures.CDownloader;
import com.visualon.AppPlayerCommonFeatures.CPlayer;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.Definition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public  class WebBrowserFragment extends Fragment  {
    

    private static final String PREFIX_LOCALFILE                   = "file://";
    private static final String TAG                                = "WebBrowserActivity.java";
    private long                                m_nlastClickTime = 0;
    private Button                              m_btnGoForward;
    private Button                              m_btnGoBack;
    private ProgressBar                         m_pbUrl;
    private AutoCompleteTextView                m_actvUrl;
    private ImageView                           m_ivPlay;
    private ImageButton                         m_ibHistory;
    private WebView                             m_webView;
    private MessageHandler                      m_Handler;
    private CDownloader        m_cDownloader          = null;
    private CPlayer            m_cPlayer              = null;
    
    private static final int MSG_LOCALFILE_GENERATED = 1;
    private StringBuilder    m_sbLocalFile = new StringBuilder();
    
    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        m_Handler = new MessageHandler();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.web, null);
        m_actvUrl = (AutoCompleteTextView)v.findViewById(R.id.actvUrl);
        m_pbUrl = (ProgressBar)v.findViewById(R.id.pbUrl);
        m_btnGoForward = (Button)v.findViewById(R.id.btnGo);
        m_btnGoBack = (Button)v.findViewById(R.id.btnBack);
        m_ibHistory = (ImageButton)v.findViewById(R.id.ibHistory);
        m_ivPlay = (ImageView)v.findViewById(R.id.ivPlay);
        m_webView = (WebView)v.findViewById(R.id.webView);
        m_webView.getSettings().setJavaScriptEnabled(true);
        //Don't show Horizontal & Vertical ScrollBar
        m_webView.setHorizontalScrollBarEnabled(false); 
        m_webView.setVerticalScrollBarEnabled(false);
        m_webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        m_webView.getSettings().setBuiltInZoomControls(true); 
        m_webView.getSettings().setSupportZoom(true); 
        m_webView.setWebViewClient(new VOWebViewClient());
        m_webView.setWebChromeClient(new VOWebChromeClient());
        m_cPlayer = CommonFunc.getCPlayer();
        m_cDownloader = CommonFunc.getCDownloader();
        initLayout();
        searchLocalFile();
      
        return v;
    }
    
    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        	case MSG_LOCALFILE_GENERATED:
        		 if (m_webView != null) {
        			 loadUrl(Definition.LOCAL_FILE_BROWSER_PATH);
        		 }
        		break;
        	default:
        		break;
        	}
            super.handleMessage(msg);
        }
    }
    
    private void initLayout(){
        prepareButtonHistory();
        prepareButtonGo();
        prepareButtonBack();
        prepareButtonPlay();
        prepareAutoCompleteTextView();
    }
    private String processUrl(String url) {
        
        if (CommonFunc.checkFileExt(url)) {
            m_cPlayer.setPlayerURL(url);
            startPlayer();
        } else {
            String strLoadUrl = url.toLowerCase();
            if (strLoadUrl.startsWith(Definition.PREFIX_HTTP) == false 
                && strLoadUrl.startsWith(Definition.PREFIX_HTTPS) == false
                && strLoadUrl.startsWith(Definition.PREFIX_RTSP) == false
                && strLoadUrl.startsWith(PREFIX_LOCALFILE) == false) {
                url = Definition.PREFIX_HTTP + url;
            }
            loadUrl(url);
        }
        return url;
    }
    private void prepareButtonHistory(){
       
        m_ibHistory.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                
                startActivity(intent);  


            }

        });
    }
    
    private void prepareButtonGo(){
        m_btnGoForward.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m_webView.canGoForward()) {
                    String currentUrl = m_webView.getUrl();
                    WebBackForwardList mWebBackForwardList = m_webView.copyBackForwardList();
                    String url=currentUrl;
                    url = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()+1).getUrl();
                    m_webView.goForward();
                    setActvText(url);
                 }  
            }
        });
       
    }
    
    private void prepareButtonBack(){
        m_btnGoBack.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m_webView.canGoBack()) {
                    String currentUrl = m_webView.getUrl();
                    WebBackForwardList mWebBackForwardList = m_webView.copyBackForwardList();
                    String url=currentUrl;
                    if (mWebBackForwardList.getCurrentIndex() > 0) 
                        url = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()-1).getUrl();
                    m_webView.goBack();
                    setActvText(url);
                 }  
            }
        });
       
    }
    
    private void prepareButtonPlay() {

       
        m_ivPlay.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String url = m_actvUrl.getText().toString();
                url = processUrl(url);
                addUrlToHistroy(url);
                InputMethodManager imm =(InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
                imm.hideSoftInputFromWindow(m_actvUrl.getWindowToken(), 0);
                
            }
        });
    }
    private void prepareAutoCompleteTextView() {
          m_actvUrl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                 ArrayList<String> lstUrl = new ArrayList<String>();
                for (int i = 0; i < Definition.PREFIX_SUGGEST.length; i++) 
                    lstUrl.add(Definition.PREFIX_SUGGEST[i]);

            if (lstUrl.size() != 0) {
                    ArrayAdapter<String> aa = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                            R.layout.webbrowser_url_dropdown_item,
                            (String[]) lstUrl.toArray(new String[lstUrl.size()]));
                    m_actvUrl.setAdapter(aa);
                }
            }
        });
    
        m_actvUrl.setOnEditorActionListener(new TextView.OnEditorActionListener()   
        {   
            public boolean onEditorAction(TextView v, int actionId,   
                    KeyEvent event)   
            {   
                if (actionId == EditorInfo.IME_ACTION_GO)   
                {   
                    String url = m_actvUrl.getText().toString();
                    url = processUrl(url);
                    addUrlToHistroy(url); 
                    InputMethodManager imm =(InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
                    imm.hideSoftInputFromWindow(m_actvUrl.getWindowToken(), 0);
                }  
                return true;    
            }   
        }); 
    }

    private class VOWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            final int p = progress;
            m_Handler.post(new Runnable() {
                public void run() {
                   m_pbUrl.setProgress(p);
                }
            });
           m_pbUrl.invalidate();   
            int max = m_pbUrl.getMax();
            if (progress == max) {
                m_Handler.post(new Runnable() {
                    public void run() {
                       m_pbUrl.setProgress(0);
                    }
                });
            }
        }
    }

    private class VOWebViewClient extends WebViewClient {
    
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {

         
        if (CommonFunc.checkFileExt(url)) {
            if(!isFastDoubleClick())
            {
                m_cPlayer.setPlayerURL(url);
                startPlayer();
            }
        } else {

            if ( (url.startsWith(PREFIX_LOCALFILE)  // PREFIX_LOCALFILE = "file://"
                    && CommonFunc.isLink(url) == false ) ) { // local file mode
                } else {                                // internet mode
                setActvText(url);
                view.loadUrl(url);
            }
        }

        return true;
    }
    
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
            SslError error) {
        handler.proceed();
    }
   }
    public boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - m_nlastClickTime;
        m_nlastClickTime = time; 
        if ( 0 < timeD && timeD < 2500) {   
            return true;   
        }   
        return false;   
    }
    
    private void loadUrl(String url) {
        if (m_webView != null && m_actvUrl != null 
                && url != null && url.length() != 0) {
            m_webView.loadUrl(url);
            setActvText(url);
        }
    }
    
    private void setActvText(String strText) {
        if (strText == null)
            return;
        
        ArrayAdapter<String> aa = null;
        m_actvUrl.setAdapter(aa);
        m_actvUrl.setText(strText);
        m_actvUrl.setSelection(strText.length());
    }
   
    protected boolean isDownloadEnable(){
        boolean b = (m_cPlayer.getBehavior().getOptionItemByID(OPTION_ID.OPTION_DOWNLOAD_ID.getValue()).getSelect()==1);
        return b;
    }
    
    @Override
    public void onResume(){
        super.onResume();
        
        String url = getActivity().getIntent().getStringExtra("URLNAME");
        getActivity().getIntent().putExtra("URLNAME", "");
        if (url != null && url.length()>0){
            processUrl(url);
        }
    }
    
    private void addUrlToHistroy(String url) {

        String fileName;
        if (CommonFunc.checkSDCard()) {
            fileName = Definition.DOWNLOAD_PATH + "/"
                    + Definition.DEFAULT_HISTORYLIST_FILENAME;
        } else {
            fileName = Definition.DEFAULT_DATAFILE_PATH + getActivity().getApplicationContext().getPackageName() + "/"
                    + Definition.DEFAULT_HISTORYLIST_FILENAME; // "/data/data/PackageName/history.txt";
        }

        CommonFunc.addStringToFile(fileName, url);

    }

  private void startPlayer(){
      Intent intent = new Intent();
      m_cPlayer.createPlayer();
      if(isDownloadEnable()){
          m_cDownloader.createDownloader();
          }
      checkDRM();
      intent.setClass(getActivity().getApplicationContext(), BasePlayer.class);
      startActivity(intent);
  }
    
  private void checkDRM(){
      FeatureManager featureManager = new FeatureManager(getActivity());
      if(isDownloadEnable()){
          featureManager.setupDRM(m_cPlayer, m_cDownloader);
      } else {
          featureManager.setupDRM(m_cPlayer, null);
      }
  }
  
  private void searchLocalFile() {
	  setHTMLheadtag();
	  getLocalFileList();
	  setHTMLfoottag();
	  writeHTML(Definition.LOCAL_FILE_BROWSER_PATH);
	  m_Handler.sendEmptyMessage(MSG_LOCALFILE_GENERATED);
  }
  
  private void getLocalFileList() {
	  
	  ArrayList<localFileDirectory> localFileDirectoryList = new ArrayList<localFileDirectory>();
	  localFileDirectory fileDirectory;
	  
	  File rootFile = new File("/sdcard");
	  File[] fileList= rootFile.listFiles();
	  if (fileList == null)
		  return;
	  fileDirectory = new localFileDirectory("/sdcard");
	  for (File file: fileList) {
		  if (CommonFunc.checkFileExt(file.getName()))
			  fileDirectory.addSubPlaybackFile(file.getName());
	  }
	  if (fileDirectory.getSubfileList().size() > 0)
	      localFileDirectoryList.add(fileDirectory);
	  
	  for (int i=0; i<fileList.length;i++ ) {
		  if (fileList[i].isDirectory()) {
			  File[] subfileList= fileList[i].listFiles();
			  if (subfileList == null)
			  return;
			  localFileDirectory subFileDirectory = new localFileDirectory(fileList[i].getPath());
			  for (File file: subfileList) {
				  if (CommonFunc.checkFileExt(file.getName()))
					  subFileDirectory.addSubPlaybackFile(file.getName());
			  }
			  if (subFileDirectory.getSubfileList().size() > 0)
			      localFileDirectoryList.add(subFileDirectory);
		  }
	  }
	  if (localFileDirectoryList.size() == 0)
		  return;
	  m_sbLocalFile.append("<div id=\"outer\">\n");
	  m_sbLocalFile.append("<ul id=\"tab\">\n");
	  
	  m_sbLocalFile.append("<li class=\"current\">" + localFileDirectoryList.get(0).getFilePath() + "</li>\n");
	  if (localFileDirectoryList.size() > 2) {
		  for (int j=1;j<localFileDirectoryList.size(); j++) {
			  m_sbLocalFile.append("<li>" + localFileDirectoryList.get(j).getFilePath() + "</li>\n");
		  }
	  }
	
	  m_sbLocalFile.append("</ul>\n");
	  m_sbLocalFile.append("<div id=\"content\">\n");
	  for (int n=0;n <localFileDirectoryList.size();n++ ) {
		  ArrayList<String> subFielList = localFileDirectoryList.get(n).getSubfileList();
		  String filePath = localFileDirectoryList.get(n).getFilePath();
		  m_sbLocalFile.append("<ul>\n");
		  for (int m = 0; m < subFielList.size(); m ++) {
			  String subFileName = subFielList.get(m);
			  m_sbLocalFile.append("<a href=\"" + filePath + "/" + subFileName + "\">" + subFileName + "</a><br>\n");
		  }
		
		  m_sbLocalFile.append("</ul>\n");
	  }
	
	  m_sbLocalFile.append("</div>\n");
	  m_sbLocalFile.append("</div>\n");
	  localFileDirectoryList.clear();
  }
  
  private void setHTMLheadtag() {
	  m_sbLocalFile.append("<!doctype html>\n");
	  m_sbLocalFile.append("<html lang=\"en\">\n");
	  m_sbLocalFile.append("<head>\n");
	  m_sbLocalFile.append("<meta charset=\"UTF-8\">\n");
	  m_sbLocalFile.append("<title>Local</title>\n");
	  m_sbLocalFile.append("<style>\n");
	  m_sbLocalFile.append("*{ margin:0; padding:0;list-style: none;}\n");
	  m_sbLocalFile.append("body {font:12px/1.5 Tahoma;}\n");
	  m_sbLocalFile.append("#outer {width:450px;margin:150px auto;}\n");
	  m_sbLocalFile.append("#tab {overflow:hidden;zoom:1;background:#000;border:1px solid #000;}\n");
	  m_sbLocalFile.append("#tab li {color:#fff;height:30px;line-height:30px;padding:0 20px;cursor:pointer;float:left;}\n");
	  m_sbLocalFile.append("#tab li.current {color:#000;background:#ccc;}\n");
	  m_sbLocalFile.append("#content {border:1px solid #000;border-top-width:0;}\n");
	  m_sbLocalFile.append("#content ul {line-height:25px;display:none;	margin:0 30px;padding:10px 0;}\n");
	  m_sbLocalFile.append("</style>\n");
	  m_sbLocalFile.append("</head>\n");
	  m_sbLocalFile.append("<body>\n");
	  
  }
  
  private void setHTMLfoottag() {
	  m_sbLocalFile.append("<script src=\"./jquery-2.1.3.min.js\"></script>\n");
	  m_sbLocalFile.append("<script>\n");
	  m_sbLocalFile.append("$(function(){\n");
	  m_sbLocalFile.append("window.onload = function()\n");
	  m_sbLocalFile.append("{\n");
	  m_sbLocalFile.append("var $li = $('#tab li');\n");
	  m_sbLocalFile.append("var $ul = $('#content ul');\n");
	  m_sbLocalFile.append("$li.click(function(){\n");
	  m_sbLocalFile.append("var $this = $(this);\n");
	  m_sbLocalFile.append("var $t = $this.index();\n");
	  m_sbLocalFile.append("$li.removeClass();\n");
	  m_sbLocalFile.append("$this.addClass('current');\n");
	  m_sbLocalFile.append("$ul.css('display','none');\n");
	  m_sbLocalFile.append("$ul.eq($t).css('display','block');\n");
	  m_sbLocalFile.append("})\n");
	  m_sbLocalFile.append("$ul.eq(0).css('display','block');\n");
	  m_sbLocalFile.append("}\n");
	  m_sbLocalFile.append("});\n");
	  m_sbLocalFile.append("</script>\n");
	  m_sbLocalFile.append("</body>\n");
	  m_sbLocalFile.append("</html>\n");
  }
  
  private void writeHTML(String strPathName) {
	  String strHTMLFilePath = strPathName.startsWith(PREFIX_LOCALFILE) ?
	                strPathName.substring(PREFIX_LOCALFILE.length()) : strPathName;

	  File dir = new File(strHTMLFilePath);
		try {
			Writer mWriteOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir), "UTF8"));
			mWriteOut.write(m_sbLocalFile.toString());
			mWriteOut.flush();
			mWriteOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

      m_sbLocalFile.setLength(0);
  }
  
  public class localFileDirectory {
	  private String mFilePath;
	  private ArrayList<String> mSubFileList;
	  
	  public localFileDirectory(String filePath) {
		  mFilePath = filePath;
		  mSubFileList = new ArrayList<String>();
	  }
	  
	  public String getFilePath() {
		  return mFilePath;
	  }
	  
	  public void addSubPlaybackFile(String subFileName) {
		  mSubFileList.add(subFileName);
	  }
	  
	  public ArrayList<String> getSubfileList() {
		  return mSubFileList;
	  }
  }
}
