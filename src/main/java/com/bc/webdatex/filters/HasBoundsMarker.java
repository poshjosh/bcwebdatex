package com.bc.webdatex.filters;

import com.bc.webdatex.bounds.BoundsMarker;

public abstract interface HasBoundsMarker
{
  public abstract BoundsMarker getBoundsMarker();
  
  public abstract void setBoundsMarker(BoundsMarker paramBoundsMarker);
}
