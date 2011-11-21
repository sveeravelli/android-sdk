package com.ooyala.android;

import java.util.ArrayList;

public class ChannelSet extends ContentItem
{
  protected ArrayList<Channel>_channels = new ArrayList<Channel>();

  protected void addChannel(Channel channel)
  {
    _channels.add(channel);
  }

  public ArrayList<Channel> getChannels()
  {
    return _channels;
  }
}
