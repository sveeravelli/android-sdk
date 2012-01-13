#!/bin/bash

echo

echo "Removing Old Docs..."
rm -rf Documentation/complete Documentation/public

echo "Generating Public Docs..."
javadoc -sourcepath sdk/src \
        -d Documentation/public -use -windowtitle 'Ooyala Android SDK API Documentation' \
        -doctitle 'Ooyala Android SDK API Documentation' \
        -header '<b>Ooyala Android SDK' \
        -bottom '<font size="-1">Copyright 2012 Ooyala, Inc. All Rights Reserved.</font>' \
        -link http://docs.oracle.com/javase/6/docs/api/ \
        -public \
        com.ooyala.android com.ooyala.android.player

echo "Generating Complete Docs..."
javadoc -sourcepath sdk/src \
        -d Documentation/complete -use -windowtitle 'Ooyala Android SDK API Documentation' \
        -doctitle 'Ooyala Android SDK API Documentation' \
        -header '<b>Ooyala Android SDK' \
        -bottom '<font size="-1">Copyright 2012 Ooyala, Inc. All Rights Reserved.</font>' \
        -link http://docs.oracle.com/javase/6/docs/api/ \
        com.ooyala.android com.ooyala.android.player

echo
echo "Document Generation Complete!"
echo
