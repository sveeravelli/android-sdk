#!/bin/bash

echo

echo "Removing Old Docs..."
rm -rf Documentation/complete Documentation/public

echo "Generating Public Docs..."
javadoc -linkoffline http://d.android.com/reference file:${ANDROID_SDKS}/docs/reference @javadoc.public

echo "Generating Complete Docs..."
javadoc -linkoffline http://d.android.com/reference file:${ANDROID_SDKS}/docs/reference @javadoc.complete

echo
echo "Document Generation Complete!"
echo
