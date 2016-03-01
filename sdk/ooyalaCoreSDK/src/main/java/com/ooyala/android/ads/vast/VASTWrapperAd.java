package com.ooyala.android.ads.vast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

class VASTWrapperAd extends VASTAd {

  private Node _childAdXML;

  VASTWrapperAd(Element data) {
    super(data);
  }

  @Override
  boolean update(Element xml) {
    Node type = xml.getFirstChild();
    while (type != null) {
      if (!(type instanceof Element)) {
        type = type.getNextSibling();
        continue;
      }

      Node child = type.getFirstChild();
      String vastAdTagURI = null;

      while (child != null) {
        if (!(child instanceof Element)) {
          child = child.getNextSibling();
          continue;
        }
        String text = child.getTextContent().trim();
        boolean textExists = text != null;
        if (textExists && ((Element) child).getTagName().equals(Constants.ELEMENT_AD_SYSTEM)) {
          _system = text;
          _systemVersion = ((Element) child).getAttribute(Constants.ATTRIBUTE_VERSION);
        } else if (textExists && ((Element) child).getTagName().equals(Constants.ELEMENT_AD_TITLE)) {
          _title = text;
        } else if (textExists && ((Element) child).getTagName().equals(Constants.ELEMENT_DESCRIPTION)) {
          _description = text;
        } else if (textExists && ((Element) child).getTagName().equals(Constants.ELEMENT_SURVEY)) {
          _surveyURLs.add(text);
        } else if (textExists && ((Element) child).getTagName().equals(Constants.ELEMENT_ERROR)) {
          _errorURLs.add(text);
        } else if (textExists && ((Element) child).getTagName().equals(Constants.ELEMENT_IMPRESSION)) {
          _impressionURLs.add(text);
        } else if (((Element) child).getTagName().equals(Constants.ELEMENT_EXTENSIONS)) {
          _extensions = (Element) child;
        } else if (((Element) child).getTagName().equals(Constants.ELEMENT_VAST_AD_TAG_URI)) {
          vastAdTagURI = text;
        } else if (((Element) child).getTagName().equals(Constants.ELEMENT_CREATIVES)) {
          Node creative = child.getFirstChild();
          while (creative != null) {
            if (creative instanceof Element) {
              addCreative((Element) creative);
            }
            creative = creative.getNextSibling();
          }
          Collections.sort(_sequence);
        }
        child = child.getNextSibling();
      }
      if (vastAdTagURI != null) {
      	try {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          DocumentBuilder db = dbf.newDocumentBuilder();
          Document doc = db.parse(new InputSource((new URL(vastAdTagURI)).openStream()));
          Element vast = doc.getDocumentElement();
          if (!vast.getTagName().equals(Constants.ELEMENT_VAST)) { return false; }
          String vastVersion = vast.getAttribute(Constants.ATTRIBUTE_VERSION);
          if (Double.parseDouble(vastVersion) < Constants.MINIMUM_SUPPORTED_VAST_VERSION) { return false; }
          Node ad = vast.getFirstChild();
          while (ad != null) {
            if ((ad instanceof Element) && ((Element) ad).getTagName().equals(Constants.ELEMENT_AD)) {
            	//Set the childAdXML to be the correct ad node instance to be called in VASTAd
              _childAdXML = ad;
            }
            ad = ad.getNextSibling();
          }
        } catch (Exception e) {
          System.out.println("ERROR: Unable to fetch VAST ad tag info: " + e);
          return false;
        }
      }
      type = type.getNextSibling();
    }
    return true;
  }

  /**
   * Fetch the XML URL of the child of the wrapper to be passed into
   * another update function in the VASTAd class
   * @return the Node which represents the child XML URL
   */
  Node getChildAdXML() {
    return _childAdXML;
  }
}
