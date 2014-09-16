#!/bin/bash

CURRDIR=`pwd`

SCRIPTDIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASEDIR=${SCRIPTDIR}/../../

cd ${BASEDIR}

${BASEDIR}/script/android-sdk setup -noavd
${BASEDIR}/script/android-sdk tests || exit $?
${BASEDIR}/script/android-sdk gen -f || exit $?

cd ${CURRDIR}
