#!/bin/bash

BEGIN_DIR=`pwd`
SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../
ZIP_BASE='Ooyala-VisualOn-Integration'
ZIP_NAME=${ZIP_NAME}.zip

cd ${BASE_DIR}
mkdir ${ZIP_BASE}
cp -r ${BASE_DIR}/vendor/SecurePlayer/assets ${BASE_DIR}/${ZIP_BASE}/
cp -r ${BASE_DIR}/vendor/SecurePlayer/GENERAL_ANDR_VOP_PROB_RC_02_00_208_1168/SecurePlayerSDK/libs ${BASE_DIR}/${ZIP_BASE}/
cp -r ${BASE_DIR}/vendor/SecurePlayer/General/SecurePlayerSDK/libs/armeabi-v7a/* ${BASE_DIR}/${ZIP_BASE}/libs/armeabi-v7a/
cp -r ${BASE_DIR}/vendor/SecurePlayer/HOW_TO_INTEGRATE_WITH_VISUALON.txt ${BASE_DIR}/${ZIP_BASE}/
cp -r ${BASE_DIR}/internal-apps/PlayreadyDeviceManagementSampleApp ${BASE_DIR}/${ZIP_BASE}/



