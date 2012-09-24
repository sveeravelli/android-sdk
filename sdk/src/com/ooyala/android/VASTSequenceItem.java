package com.ooyala.android;

import org.w3c.dom.Element;

public class VASTSequenceItem implements Comparable<VASTSequenceItem> {
  /** The sequence number associated with this item in the sequence */
  private int _number = -1;
  /** The Linear Ad associated with this item in the sequence */
  private VASTLinearAd _linear = null;
  /** The Non-Linear Ads associated with this item in the sequence (XML Element) */
  private Element _nonLinears = null;
  /** The Companion Ads associated with this item in the sequence (XML Element) */
  private Element _companions = null;

  /**
   * Whether or not this VASTSequenceItem has a linear ad
   * @return true if there exists a linear ad, false if there does not;
   */
  public boolean hasLinear() {
    return _linear != null;
  }

  /**
   * This method is used to sort Lists of VASTSequenceItems. There is no need to call this method.
   */
  @Override
  public int compareTo(VASTSequenceItem arg0) {
    if (_number < arg0.getNumber()) return -1;
    if (_number > arg0.getNumber()) return 1;
    return 0;
  }

  /**
   * Get the number of this VASTSequenceItem. Multiple VASTSequenceItems are ordered by this number.
   * @return
   */
  public int getNumber() {
    return _number;
  }

  /**
   * Set the number of this VASTSequenceItem. Multiple VASTSequenceItems are ordered by this number.
   * @param number the number to set.
   */
  public void setNumber(int number) {
    this._number = number;
  }

  /**
   * Get the VASTLinearAd in this VASTSequenceItem.
   * @return the VASTLinearAd
   */
  public VASTLinearAd getLinear() {
    return _linear;
  }

  /**
   * Set the VASTLinearAd in this VASTSequenceItem
   * @param linear the VASTLinearAd to set
   */
  public void setLinear(VASTLinearAd linear) {
    this._linear = linear;
  }

  /**
   * Get the raw XML Element object specifying the Non-Linear Ads associated with this VASTSequenceItem.
   * @return the Element object.
   */
  public Element getNonLinears() {
    return _nonLinears;
  }

  /**
   * Set the raw XML Element object specifying the Non-Linear Ads associated with this VASTSequenceItem.
   * @param nonLinears the Element object to set.
   */
  public void setNonLinears(Element nonLinears) {
    this._nonLinears = nonLinears;
  }

  /**
   * Get the raw XML Element object specifying the Companion Ads associated with this VASTSequenceItem.
   * @return the Element object.
   */
  public Element getCompanions() {
    return _companions;
  }

  /**
   * Set the raw XML Element object specifying the Companion Ads associated with this VASTSequenceItem.
   * @param nonLinears the Element object to set.
   */
  public void setCompanions(Element companions) {
    this._companions = companions;
  }
}
