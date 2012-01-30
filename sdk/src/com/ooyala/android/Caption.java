package com.ooyala.android;

import org.w3c.dom.Element;

public class Caption
{
  protected double _begin = 0;
  protected double _end = 0;
  protected String _text = null;

  Caption()
  {
  }

  Caption(Element element)
  {
    if (!element.getTagName().equals(Constants.ELEMENT_P)) { return; }

    String beginStr = element.getAttribute(Constants.ATTRIBUTE_BEGIN);
    String durationStr = element.getAttribute(Constants.ATTRIBUTE_DUR);
    String endStr = element.getAttribute(Constants.ATTRIBUTE_END);

    if (Utils.isNullOrEmpty(beginStr)) { return; }
    _begin = Utils.secondsFromTimeString(beginStr);

    if (!Utils.isNullOrEmpty(endStr))
    {
      _end = Utils.secondsFromTimeString(endStr);
    }
    else if (!Utils.isNullOrEmpty(durationStr))
    {
      _end = _begin + Utils.secondsFromTimeString(durationStr);
    }
    else
    {
      return;
    }

    _text = element.getTextContent().trim(); // TODO: May need to clean up the text
  }

  public double getBegin()
  {
    return _begin;
  }

  public double getEnd()
  {
    return _end;
  }

  public String getText()
  {
    return _text;
  }
}
