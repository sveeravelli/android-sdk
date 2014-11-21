#!/bin/bash

set -x

SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../
SP_ZIP_BASE="OoyalaSecurePlayerIntegration-${PLATFORM_NAME}"
SP_ZIP_NAME=${SP_ZIP_BASE}.zip
SP_SAMPLE_DIR=${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp
SP_SAMPLE_LIB_DIR=${SP_SAMPLE_DIR}/libs
SP_SAMPLE_ASSETS_DIR=${SP_SAMPLE_DIR}/assets

LICENSE_MD5="1a04be214fa2ffcb4c562a225cf57534"

function gen_secureplayer {
  echo "Building SecurePlayer zip"

  cd ${SP_SAMPLE_DIR}
  ant clean

  cd ${BASE_DIR}
  mkdir ${SP_ZIP_BASE}

  version=$(get_version)
  saved_rc=$(get_rc)
  git_rev=`git rev-parse HEAD`
  #SecurePlayer version file
  echo "This was built with OoyalaSDK v${version}_RC${saved_rc}" >> ${SP_ZIP_BASE}/VERSION
  echo "Git SHA: ${git_rev}" >> ${SP_ZIP_BASE}/VERSION
  echo "Created On: ${DATE}" >> ${SP_ZIP_BASE}/VERSION

  cp -r ${BASE_DIR}/vendor/SecurePlayer/assets ${BASE_DIR}/${SP_ZIP_BASE}/
  cp -r ${BASE_DIR}/vendor/SecurePlayer/SecurePlayerSDK/libs ${BASE_DIR}/${SP_ZIP_BASE}/

  mkdir -p ${SP_SAMPLE_LIB_DIR}
  mkdir -p ${SP_SAMPLE_LIB_DIR}/armeabi
  mkdir -p ${SP_SAMPLE_LIB_DIR}/armeabi-v7a
  mkdir -p ${SP_SAMPLE_ASSETS_DIR}
  cp ${BASE_DIR}/${ZIP_BASE}/${JAR_NAME} ${SP_SAMPLE_LIB_DIR}
  cp ${BASE_DIR}/${SP_ZIP_BASE}/libs/* ${SP_SAMPLE_LIB_DIR}
  cp ${BASE_DIR}/${SP_ZIP_BASE}/libs/armeabi/* ${SP_SAMPLE_LIB_DIR}/armeabi
  cp ${BASE_DIR}/${SP_ZIP_BASE}/libs/armeabi-v7a/* ${SP_SAMPLE_LIB_DIR}/armeabi-v7a
  cp ${BASE_DIR}/${SP_ZIP_BASE}/assets/* ${SP_SAMPLE_ASSETS_DIR}

  cp -r ${BASE_DIR}/vendor/SecurePlayer/HOW_TO_INTEGRATE_WITH_SECUREPLAYER.txt ${BASE_DIR}/${SP_ZIP_BASE}/
  cp -r ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp ${BASE_DIR}/${SP_ZIP_BASE}/
}

function zip_secureplayer {
  rm ${SP_ZIP_NAME}
  zip -r ${SP_ZIP_BASE} ${SP_ZIP_BASE}/*
  rm -rf ${SP_ZIP_BASE}
}

function pub_rc_secureplayer {
  echo "Publishing SecurePlayer Release Candidate"
  echo "  Copying ${SP_ZIP_NAME} to ${CANDIDATE_DIR}${SP_ZIP_NAME}"
  cp ${SP_ZIP_NAME} "${CANDIDATE_DIR}"${SP_ZIP_NAME}
}

function pub_release_secureplayer {
  echo "Moving SecurePlayer RC to Release"
  echo "  Copying ${CANDIDATE_DIR}${SP_ZIP_NAME} to ${RELEASE_DIR}${SP_ZIP_NAME}"
  cp "${CANDIDATE_DIR}"${SP_ZIP_NAME} "${RELEASE_DIR}"${SP_ZIP_NAME}
}

function verify_secureplayer {
  package_license=`md5 -q ${BASE_DIR}/${SP_ZIP_BASE}/assets/voVidDec.dat`
  if [ "$LICENSE_MD5" !=  "$package_license" ]; then
    echo "ERROR: license in SecurePlayer package is not the correct license file!"
    echo "$LICENSE_MD5 vs $package_license"
    exit 1
  fi

  echo "SecurePlayer License verified"
}
