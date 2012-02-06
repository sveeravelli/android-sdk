#!/bin/bash

CURRDIR=`pwd`

SCRIPTDIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASEDIR=${SCRIPTDIR}/../../

cd ${BASEDIR}

${BASEDIR}/script/android-sdk setup -noavd
${BASEDIR}/script/android-sdk pub -gen -nightly || exit $?

cd ${CURRDIR}
