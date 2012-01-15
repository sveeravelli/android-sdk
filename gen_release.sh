CURRDIR=`pwd`
cd sdk
ant release
cd ${CURRDIR}
cp sdk/build/dist/OoyalaSDK-Android.zip .
