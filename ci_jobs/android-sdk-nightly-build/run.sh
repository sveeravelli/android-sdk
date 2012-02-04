#!/bin/bash

CURRDIR=`pwd`

SCRIPTDIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASEDIR=${SCRIPTDIR}/../../

cd ${BASEDIR}
git pull

${BASEDIR}/script/android-sdk pub -gen -nightly || exit $?

git stash
git stash drop

cd ${CURRDIR}
