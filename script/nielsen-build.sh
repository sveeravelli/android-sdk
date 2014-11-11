#!/bin/bash

SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../
APP_DIR=${BASE_DIR}/third_party_sample_apps/NielsenSampleApp
LICENSE_MD5="1a04be214fa2ffcb4c562a225cf57534"

function gen_nielsen {
  echo "Building Nielsen"

  cd ${BASE_DIR}

  # Note: mips is supported by Nielsen, but not by VisualOn.
  rm -rf ${APP_DIR}/{assets,libs}
  mkdir -p ${APP_DIR}/{assets,libs}
  mkdir -p ${APP_DIR}/libs/{armeabi,armeabi-v7a,x86}
  # VisualOn
  cp ${BASE_DIR}/vendor/VisualOn/Assets/* ${APP_DIR}/assets
  cp ${BASE_DIR}/vendor/VisualOn/Jar/*.jar ${APP_DIR}/libs
  cp ${BASE_DIR}/vendor/VisualOn/Libs/*.so ${APP_DIR}/libs/armeabi
  cp ${BASE_DIR}/vendor/VisualOn/Libs_x86/*.so ${APP_DIR}/libs/x86
  # Nielsen
  cp ${BASE_DIR}/vendor/Nielsen/libs/*.jar ${APP_DIR}/libs
  cp -r ${BASE_DIR}/vendor/Nielsen/libs/armeabi ${APP_DIR}/libs
  cp -r ${BASE_DIR}/vendor/Nielsen/libs/armeabi-v7a ${APP_DIR}/libs
  cp -r ${BASE_DIR}/vendor/Nielsen/libs/x86 ${APP_DIR}/libs
}

function verify_nielsen {
  package_license=`md5 -q ${APP_DIR}/assets/voVidDec.dat`
  if [ "$LICENSE_MD5" !=  "$package_license" ]; then
    echo "ERROR: VisualOn license in Nielsen package is not the correct VisualOn license file!"
    echo "$LICENSE_MD5 vs $package_license"
    exit 1
  else
    echo "License verified"
  fi
}
