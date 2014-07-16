#!/bin/bash

SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../
SP_ZIP_BASE="OoyalaSecurePlayerIntegration-${PLATFORM_NAME}"
SP_ZIP_NAME=${SP_ZIP_BASE}.zip


function gen_secureplayer {
  echo "Building SecurePlayer zip"
  cp ${BASE_DIR}/${ZIP_BASE}/${JAR_NAME} ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp/libs/

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
  cp -r ${BASE_DIR}/vendor/SecurePlayer/HOW_TO_INTEGRATE_WITH_SECUREPLAYER.txt ${BASE_DIR}/${SP_ZIP_BASE}/
  cp -r ${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp ${BASE_DIR}/${SP_ZIP_BASE}/

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
