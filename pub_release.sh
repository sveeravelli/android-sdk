#!/bin/bash

if [[ "$1" = "-gen" || "$1" = "-g" ]]; then
  ./gen_release.sh
fi

if [[ "`ls ~/Documents/Box\ Documents/X-Device/SDKs/Android/ |grep OoyalaSDK-Android`" != "" ]]; then
  rm ~/Documents/Box\ Documents/X-Device/SDKs/Android/OoyalaSDK-Android*
fi

VERSION=`cat sdk/src/com/ooyala/android/Constants.java |grep "SDK_VERSION" |awk '{print $7}' |sed 's/"\([0-9]*\.[0-9]*\.[0-9]*\)";/\1/'`
DATE=`date +%Y%m%d%H%M%S`
cp OoyalaSDK-Android.zip ~/Documents/Box\ Documents/X-Device/SDKs/Android/OoyalaSDK-Android-${VERSION}-${DATE}.zip
cp OoyalaSDK-Android.zip ~/Documents/Box\ Documents/X-Device/SDKs/Android/Versions/OoyalaSDK-Android-${VERSION}-${DATE}.zip
cp OoyalaSDK-Android.zip ~/Documents/Box\ Documents/X-Device/SDKs/Android/OoyalaSDK-Android.zip
