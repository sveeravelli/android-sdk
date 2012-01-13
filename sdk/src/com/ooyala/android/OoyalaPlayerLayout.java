package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

public class OoyalaPlayerLayout extends RelativeLayout {
  private List<Observer> _observers = new ArrayList<Observer>();

  public OoyalaPlayerLayout(Context context) {
    super(context);
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void addObserver(Observer o) {
    if (_observers.contains(o)) { return; }
    _observers.add(o);
  }

  public void deleteObserver(Observer o) {
    _observers.remove(o);
  }

  private void notifyObservers() {
    for (Observer o : _observers) {
      o.update(null, this);
    }
  }

  @Override
  protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
    Log.d(this.getClass().getName(), "TEST - onSizeChanged - "+xNew+","+yNew+" "+xOld+","+yOld);
    super.onSizeChanged(xNew, yNew, xOld, yOld);
    notifyObservers();
  }
}
