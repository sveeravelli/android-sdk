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

  @Override
  public int compareTo(VASTSequenceItem arg0) {
    if (_number < arg0.getNumber()) return -1;
    if (_number > arg0.getNumber()) return 1;
    return 0;
  }

  public int getNumber() {
    return _number;
  }

  public void setNumber(int number) {
    this._number = number;
  }

  public VASTLinearAd getLinear() {
    return _linear;
  }

  public void setLinear(VASTLinearAd linear) {
    this._linear = linear;
  }

  public Element getNonLinears() {
    return _nonLinears;
  }

  public void setNonLinears(Element nonLinears) {
    this._nonLinears = nonLinears;
  }

  public Element getCompanions() {
    return _companions;
  }

  public void setCompanions(Element companions) {
    this._companions = companions;
  }
}
