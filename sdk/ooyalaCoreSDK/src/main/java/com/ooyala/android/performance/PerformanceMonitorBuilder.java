package com.ooyala.android.performance;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

final public class PerformanceMonitorBuilder {

  private final Observable observable;
  private final Set<PerformanceEventWatchInterface> eventWatches;

  public PerformanceMonitorBuilder( final Observable observable ) {
    this.observable = observable;
    this.eventWatches = new HashSet<PerformanceEventWatchInterface>();
  }

  public void addEventWatch( final PerformanceEventWatchInterface eventWatch ) {
    eventWatches.add( eventWatch );
  }

  public PerformanceMonitor build() {
    return new PerformanceMonitor( eventWatches, observable );
  }
}
