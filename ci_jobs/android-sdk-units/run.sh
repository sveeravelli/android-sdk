#!/bin/bash

CURRDIR=`pwd`

SCRIPTDIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASEDIR=${SCRIPTDIR}/../../

cd ${BASEDIR}
git pull

${BASEDIR}/script/android-sdk tests || exit $?

cd ${CURRDIR}
