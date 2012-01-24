#!/bin/bash

cd tests
adb shell am instrument -w com.ooyala.android.testapp.test/android.test.InstrumentationTestRunner
cd ..
