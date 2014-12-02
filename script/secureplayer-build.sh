#!/bin/bash

SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/..
SP_ZIP_BASE="OoyalaSecurePlayerIntegration-${PLATFORM_NAME}"
SP_ZIP_NAME=${SP_ZIP_BASE}.zip
SP_VENDOR_DIR=${BASE_DIR}/vendor/SecurePlayer
SP_SAMPLE_DIR=${BASE_DIR}/third_party_sample_apps/SecurePlayerSampleApp
LICENSE_MD5="1a04be214fa2ffcb4c562a225cf57534"

function gen_secureplayer {
  # these calls should be kept in this order.
  cd ${BASE_DIR}
  mkdir ${SP_ZIP_BASE}
  echo "Building SecurePlayer zip"
  sp_ant_sample_app
  sp_gen_version_file
  sp_copy_assets_and_libs_to_zip
  sp_copy_assets_and_libs_to_sample_app
  sp_copy_others_to_zip
}

function sp_ant_sample_app {
  echo "(Cleaning out old '`basename ${SP_SAMPLE_DIR}`' build results)"
  pushd ${SP_SAMPLE_DIR}
  ant clean
  popd
}

function sp_gen_version_file {
  version=$(get_version)
  saved_rc=$(get_rc)
  git_rev=`git rev-parse HEAD`
  #SecurePlayer version file
  echo "This was built with OoyalaSDK v${version}_RC${saved_rc}" >> ${SP_ZIP_BASE}/VERSION
  echo "Git SHA: ${git_rev}" >> ${SP_ZIP_BASE}/VERSION
  echo "Created On: ${DATE}" >> ${SP_ZIP_BASE}/VERSION
}

function cp_dir {
    SRC=$1
    DST=$2
    if ! [  -e ${DST} ] ; then
        mkdir -p ${DST}
    fi
    cp -rf ${SRC} `dirname ${DST}`
}

function copy_assets_and_libs_to_dst {
  for me in assets General/SecurePlayerSDK/libs SecurePlayerSDK/libs; do
      cp_dir ${SP_VENDOR_DIR}/${me} ${1}/`basename ${me}`
  done
}

function sp_copy_assets_and_libs_to_zip {
  copy_assets_and_libs_to_dst ${BASE_DIR}/${SP_ZIP_BASE}
}

function sp_copy_assets_and_libs_to_sample_app {
  copy_assets_and_libs_to_dst ${SP_SAMPLE_DIR}
  # also add in the (presumably just-built-by-other-scripts) Ooyala SDK.
  cp ${BASE_DIR}/${ZIP_BASE}/${JAR_NAME} ${SP_SAMPLE_DIR}/libs/
}

function sp_copy_others_to_zip {
  cp -r ${SP_VENDOR_DIR}/HOW_TO_INTEGRATE_WITH_SECUREPLAYER.txt ${BASE_DIR}/${SP_ZIP_BASE}/
  cp -r ${SP_SAMPLE_DIR} ${BASE_DIR}/${SP_ZIP_BASE}/
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
