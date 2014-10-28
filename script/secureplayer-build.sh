#!/bin/bash

SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../
SP_ZIP_BASE="OoyalaSecurePlayerIntegration-${PLATFORM_NAME}"
SP_ZIP_NAME=${SP_ZIP_BASE}.zip

LICENSE_MD5="7e9d73349dd632c818ddffece0669c22"

function gen_secureplayer {
  echo "Building SecurePlayer zip"

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
  cp -r ${BASE_DIR}/vendor/SecurePlayer/GENERAL_ANDR_VOP_PROB_RC_02_00_208_1168/SecurePlayerSDK/libs ${BASE_DIR}/${SP_ZIP_BASE}/
  cp -r ${BASE_DIR}/vendor/SecurePlayer/SIGNATURES_ANDR_VOP_PROB_RC_02_00_208_1168/SecurePlayerSDK/libs ${BASE_DIR}/${SP_ZIP_BASE}/

  mkdir ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/libs
  mkdir ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/libs/armeabi
  mkdir ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/assets

  #Copy libs for sample app
  cp ${BASE_DIR}/${ZIP_BASE}/${JAR_NAME} ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/libs/
  cp ${BASE_DIR}/${SP_ZIP_BASE}/libs/* ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/libs/
  cp ${BASE_DIR}/${SP_ZIP_BASE}/libs/armeabi/* ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/libs/armeabi/

  #Copy assets for sample app
  cp ${BASE_DIR}/${SP_ZIP_BASE}/assets/* ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/assets/

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
