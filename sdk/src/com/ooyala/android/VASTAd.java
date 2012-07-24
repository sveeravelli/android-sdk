package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class VASTAd {
  /** the ID of the Ad */
  private String _adID;
  /** the System */
  private String _system;
  /** the System Version */
  private String _systemVersion;
  /** the title of the Ad */
  private String _title;
  /** the description of the Ad */
  private String _description;
  /** the survey URLs of the Ad */
  private List<String> _surveyURLs = new ArrayList<String>();
  /** the error URLs of the Ad */
  private List<String> _errorURLs = new ArrayList<String>();
  /** the impression URLs of the Ad */
  private List<String> _impressionURLs = new ArrayList<String>();
  /** the ordered sequence of the Ad (List of VASTSequenceItem) */
  private List<VASTSequenceItem> _sequence = new ArrayList<VASTSequenceItem>();
  /** the extensions of the Ad */
  private Element _extensions;

  /**
   * Initialize a VASTAd using the specified xml (subclasses should override this)
   * @param data the Element containing the xml to use to initialize this VASTAd
   */
  VASTAd(Element data) {
    if (!data.getTagName().equals(Constants.ELEMENT_AD)) { return; }
    _adID = data.getAttribute(Constants.ATTRIBUTE_ID);
    update(data);
  }

  /**
   * Update the VASTAd using the specified xml (subclasses should override this)
   * @param xml the TBXMLElement containing the xml to use to update this VASTAd
   * @return YES if the XML was properly formatter, NO if not
   */
  boolean update(Element xml) {
    Node type = xml.getFirstChild();
    boolean found = false;
    while (type != null) {
      if (!(type instanceof Element)) {
        type = type.getNextSibling();
        continue;
      }
      boolean isInLine = ((Element) type).getTagName().equals(Constants.ELEMENT_IN_LINE);
      boolean isWrapper = ((Element) type).getTagName().equals(Constants.ELEMENT_WRAPPER);
      if (isInLine || isWrapper) {
        found = true;
        String vastAdTagURI = null;
        Node child = type.getFirstChild();
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
          } else if (isWrapper && ((Element) child).getTagName().equals(Constants.ELEMENT_VAST_AD_TAG_URI)) {
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
              if (!(ad instanceof Element) || !((Element) ad).getTagName().equals(Constants.ELEMENT_AD)) {
                ad = ad.getNextSibling();
                continue;
              }
              if (_adID.equals(((Element) ad).getAttribute(Constants.ATTRIBUTE_ID))) {
                if (update((Element) ad)) {
                  break;
                } else {
                  return false;
                }
              }
              ad = ad.getNextSibling();
            }
          } catch (Exception e) {
            System.out.println("ERROR: Unable to fetch VAST ad tag info: " + e);
            return false;
          }
        }
      }
      type = type.getNextSibling();
    }
    return found;
  }

  /**
   * Add a Creative TBXMLElement NOTE: this assumes that the element is in fact a creative.
   * @param creative the creative to add
   */
  private void addCreative(Element creative) {
    Node type = creative.getFirstChild();
    while (type != null) {
      if (type == null || !(type instanceof Element)) {
        type = type.getNextSibling();
        continue;
      };
      String sequenceNumStr = creative.getAttribute(Constants.ATTRIBUTE_SEQUENCE);
      VASTLinearAd ad = null;
      Element nonLinears = null;
      Element companions = null;
      if (((Element) type).getTagName().equals(Constants.ELEMENT_LINEAR)) {
        ad = new VASTLinearAd((Element) type);
      } else if (((Element) type).getTagName().equals(Constants.ELEMENT_NON_LINEAR_ADS)) {
        nonLinears = (Element) type;
      } else if (((Element) type).getTagName().equals(Constants.ELEMENT_COMPANION_ADS)) {
        companions = (Element) type;
      }
      if (ad == null && nonLinears == null && companions == null) { return; }
      if (sequenceNumStr != null && sequenceNumStr.length() > 0) {
        int sequenceNum = Integer.parseInt(sequenceNumStr);
        boolean added = false;
        for (VASTSequenceItem item : _sequence) {
          int currentSequenceNum = item.getNumber();
          if (currentSequenceNum == sequenceNum) {
            if (ad != null) {
              item.setLinear(ad);
            } else if (nonLinears != null) {
              item.setNonLinears(nonLinears);
            } else if (companions != null) {
              item.setCompanions(companions);
            }
            added = true;
            break;
          }
        }
        if (!added) {
          VASTSequenceItem item = new VASTSequenceItem();
          item.setNumber(sequenceNum);
          if (ad != null) {
            item.setLinear(ad);
          } else if (nonLinears != null) {
            item.setNonLinears(nonLinears);
          } else if (companions != null) {
            item.setCompanions(companions);
          }
          _sequence.add(item);
        }
      } else {
        VASTSequenceItem item = new VASTSequenceItem();
        item.setNumber(_sequence.size());
        if (ad != null) {
          item.setLinear(ad);
        } else if (nonLinears != null) {
          item.setNonLinears(nonLinears);
        } else if (companions != null) {
          item.setCompanions(companions);
        }
        _sequence.add(item);
      }
      type = type.getNextSibling();
    }
  }

  public String getAdID() {
    return _adID;
  }

  public String getSystem() {
    return _system;
  }

  public String getSystemVersion() {
    return _systemVersion;
  }

  public String getTitle() {
    return _title;
  }

  public String getDescription() {
    return _description;
  }

  public List<String> getSurveyURLs() {
    return _surveyURLs;
  }

  public List<String> getErrorURLs() {
    return _errorURLs;
  }

  public List<String> getImpressionURLs() {
    return _impressionURLs;
  }

  public List<VASTSequenceItem> getSequence() {
    return _sequence;
  }

  public Element getExtensions() {
    return _extensions;
  }
}
