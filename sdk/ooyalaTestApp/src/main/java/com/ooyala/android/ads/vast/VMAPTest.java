package com.ooyala.android.ads.vast;

import android.test.AndroidTestCase;

import com.ooyala.android.TestConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by zchen on 3/10/16.
 */
public class VMAPTest extends AndroidTestCase {
  static final String url1 =
      "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&video_doc_id=short_onecue&cmsid=496";
  static final String url2 =
      "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=midroll&pod=2&mridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&video_doc_id=short_onecue&cmsid=496";
  static final String url3 =
      "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&video_doc_id=short_onecue&cmsid=496";
  public void testPreMidPost() {
    int duration = 365*24*3600*1000;
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = TestConstants.getTestAssetAsStream(getContext(), TestConstants.TEST_VMAP_PREMIDPOST);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      VASTAdSpot adSpot = new VASTAdSpot(0, duration, adXML);
      assertNotNull(adSpot.getVMAPAdSpots());
      assertEquals(adSpot.getVMAPAdSpots().size(), 4);

      // adspot 1
      VASTAdSpot vast = adSpot.getVMAPAdSpots().get(0);
      assertTrue(vast instanceof VMAPAdSpot);
      VMAPAdSpot vmap = (VMAPAdSpot)vast;
      assertEquals(0, vast.getTime());
      assertEquals("preroll", vmap.getBreakId());
      assertEquals("linear", vmap.getBreakType());
      assertEquals("preroll-ad-1", vmap.getAdSourceId());
      assertFalse(vmap.getAllowMultipleAds());
      assertTrue(vmap.getFollowRedirects());
      assertEquals(url1, vmap.getVASTURL().toString());

      vast = adSpot.getVMAPAdSpots().get(1);
      assertTrue(vast instanceof VMAPAdSpot);
      vmap = (VMAPAdSpot)vast;
      assertEquals(15000, vast.getTime());
      assertEquals("midroll-1", vmap.getBreakId());
      assertEquals("linear", vmap.getBreakType());
      assertEquals("midroll-1-ad-1", vmap.getAdSourceId());
      assertFalse(vmap.getAllowMultipleAds());
      assertTrue(vmap.getFollowRedirects());
      assertEquals(url2, vmap.getVASTURL().toString());

      vast = adSpot.getVMAPAdSpots().get(2);
      assertTrue(vast instanceof VMAPAdSpot);
      vmap = (VMAPAdSpot)vast;
      assertEquals((int)(duration * 0.2), vast.getTime());
      assertEquals("midroll-2", vmap.getBreakId());
      assertEquals("linear", vmap.getBreakType());
      assertEquals("midroll-2-ad-1", vmap.getAdSourceId());
      assertFalse(vmap.getAllowMultipleAds());
      assertTrue(vmap.getFollowRedirects());
      assertEquals(url2, vmap.getVASTURL().toString());

      vast = adSpot.getVMAPAdSpots().get(3);
      assertTrue(vast instanceof VMAPAdSpot);
      vmap = (VMAPAdSpot)vast;
      assertTrue(vast.getTime() > duration);
      assertEquals("postroll", vmap.getBreakId());
      assertEquals("linear", vmap.getBreakType());
      assertEquals("postroll-ad-1", vmap.getAdSourceId());
      assertFalse(vmap.getAllowMultipleAds());
      assertTrue(vmap.getFollowRedirects());
      assertEquals(url3, vmap.getVASTURL().toString());
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      fail();
    }
  }

  public void testVASTAdData() {
    int duration = 3600*1000;
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = TestConstants.getTestAssetAsStream(getContext(), TestConstants.TEST_VMAP_VASTADDATA);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      VASTAdSpot adSpot = new VASTAdSpot(0, duration, adXML);
      assertNotNull(adSpot.getVMAPAdSpots());
      assertEquals(adSpot.getVMAPAdSpots().size(), 2);

      // adspot 1
      VASTAdSpot vast = adSpot.getVMAPAdSpots().get(0);
      assertTrue(vast instanceof VMAPAdSpot);
      VMAPAdSpot vmap = (VMAPAdSpot)vast;
      assertEquals(0, vast.getTime());
      assertEquals("mypre", vmap.getBreakId());
      assertEquals("linear", vmap.getBreakType());
      assertEquals("1", vmap.getAdSourceId());
      assertTrue(vmap.getAllowMultipleAds());
      assertTrue(vmap.getFollowRedirects());

      // check VAST attributes
      assertEquals(1, vast.getAds().size());
      VASTAd vastAd = vast.getAds().get(0);
      assertEquals("2447226.251866656", vastAd.getAdID());
      assertEquals("Ooyala Skippable Preroll", vastAd.getTitle());

      vast = adSpot.getVMAPAdSpots().get(1);
      assertTrue(vast instanceof VMAPAdSpot);
      vmap = (VMAPAdSpot)vast;
      assertEquals(10 * 60 * 1000 + 23125, vast.getTime());
      assertEquals("myid", vmap.getBreakId());
      assertEquals("linear", vmap.getBreakType());
      assertEquals("2", vmap.getAdSourceId());
      assertTrue(vmap.getAllowMultipleAds());
      assertTrue(vmap.getFollowRedirects());

      assertEquals(1, vast.getAds().size());
      vastAd = vast.getAds().get(0);
      assertEquals("2447226.251866656", vastAd.getAdID());
      assertEquals("Ooyala Skippable Midroll", vastAd.getTitle());

    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      fail();
    }
  }
}
