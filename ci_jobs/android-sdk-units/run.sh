#!/bin/bash

SCRIPTDIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASEDIR=${SCRIPTDIR}/../../

${BASEDIR}/script/android-sdk tests || exit $?
