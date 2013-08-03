#!/bin/bash

DATE=`date`
DATESTAMP=`date +%Y%m%d%H%M%S`

RETURN_FAIL=1
RETURN_SUCCESS=0

VERSION_REGEX='^-v[0-9]+.[0-9]+.[0-9]+$'
RC_REGEX='^-rc[0-9]+$'

BASE_DIR=${SCRIPT_DIR}/../
SDK_DIR=${BASE_DIR}/sdk
SAMPLE_DIR=${BASE_DIR}/sample-apps
LIB_BASE="OoyalaSDK"
ZIP_BASE="${LIB_BASE}-${PLATFORM_NAME}"
ZIP_NAME="${ZIP_BASE}.zip"

IMA_SDK_DIR=${BASE_DIR}/third_party_sdks/OoyalaIMASDK
THIRD_PARTY_SAMPLE_DIR=${BASE_DIR}/third_party_sample_apps
IMA_LIB_BASE="OoyalaIMASDK"
IMA_ZIP_BASE="${IMA_LIB_BASE}-${PLATFORM_NAME}"
IMA_ZIP_NAME="${IMA_ZIP_BASE}.zip"
USER_DIR=`cd ~; pwd`
if [[ "${BOX_DIR}" = "" ]]; then
  BOX_DIR="${USER_DIR}/Documents/Box Documents/"
  echo "BOX_DIR not set, using ${BOX_DIR}"
fi
CANDIDATE_DIR="${BOX_DIR}X-Device/SDKs/${PLATFORM_NAME}/Candidate/"
RELEASE_DIR="${BOX_DIR}X-Device/SDKs/${PLATFORM_NAME}/Release/"
VERSIONS_SUFFIX=Versions/

function usage {
  echo "$0 <task> <options>"
  echo "  tasks:"
  echo "    gen_docs|docs|doc|d       : generate the documentation"
  echo "    gen_release|gen|g         : generate the release"
  echo "      options:"
  echo "        -[notests|notest|nt]  : do not run the unit tests"
  echo "        -v<VERSION>           : update the version to <VERSION> where <VERSION> is in the form [0-9]+.[0-9]+.[0-9]+"
  echo "        -rc<CANDIDATE>        : set the release candidate number to <CANDIDATE>"
  echo "        -[push|p]             : push the generated release. Also create a tag if -v<VERSION> was specified"
  echo "    pub_release|publish|pub|p : publish the release"
  echo "      options:"
  echo "        -[candidate|rc|c]     : publish the release candidate version of the release"
  echo "        -v<VERSION>           : update the version to <VERSION> where <VERSION> is in the form [0-9]+.[0-9]+.[0-9]+"
  echo "        -[push|p]             : push the generated release. Also create a tag if -v<VERSION> was specified"
  echo "    run_tests|tests|test|t    : run the unit tests"
  custom_usage
  echo "For example, to generate, tag, and publish the release candidate for the latest version: $0 pub -rc -push"
  echo "To mark the latest release candidate as the release version: $0 pub -push"
  exit 1
}

# Generate the docs
function doc {
  doc_currdir=`pwd`
  cd ${SDK_DIR}
  echo
  echo "Removing Old Docs..."
  rm -rf Documentation/complete Documentation/public
  echo "Generating Public Docs..."
  public_docs
#  echo "Generating Complete Docs..."
#  complete_docs
  echo
  echo "Document Generation Complete!"
  echo
  cd "${doc_currdir}"
}

GIT_CHECK_RESULT=true
function git_check {
  git_currdir=`pwd`
  cd ${BASE_DIR}
  if [[ "`git status | grep "working directory clean"`" = "" ]]; then
    GIT_CHECK_RESULT=false
    echo
    git status
    echo
    echo "Your working directory is dirty (Yuck!), are you sure you want to continue? (y/n)"
    read -n 1 cont
    if [[ "${cont}" != "y" ]]; then
      echo
      echo "Please commit or stash your changes and try again."
      cd "${git_currdir}"
      exit 1
    fi
    echo
  else
    git pull
  fi
  cd "${git_currdir}"
}

function tests {
  tests_currdir=`pwd`
  echo "Running Unit Tests..."
  if [[ -f ${BASE_DIR}/test_results.txt ]]; then
    rm ${BASE_DIR}/test_results.txt
  fi
  custom_tests ${BASE_DIR}/test_results.txt
  return_val=$?
  if [[ "${return_val}" -eq "${RETURN_FAIL}" ]]; then
    echo "Tests Failed!"
    cd "${tests_currdir}"
    exit 1
  fi
  echo "Tests Passed!"
  rm ${BASE_DIR}/test_results.txt
  cd "${tests_currdir}"
}

function verify {
  verify_currdir=`pwd`
  cd ${BASE_DIR}
  if [[ ! ( -d "${ZIP_BASE}/Documentation" ) ]]; then
    echo "ERROR: docs not included"
    exit 1
  fi
  if [[ ! ( -d "${ZIP_BASE}/SampleApps" ) ]]; then
    echo "ERROR: sample apps not included"
    exit 1
  fi
  if [[ ! ( -f "${ZIP_BASE}/VERSION" ) ]]; then
    echo "ERROR: VERSION file not included"
    exit 1
  fi
  if [[ ! ( -f "${ZIP_BASE}/ReleaseNotes.txt" ) ]]; then
    echo "ERROR: ReleaseNotes.txt file not included"
    exit 1
  fi
  if [[ ! ( -f "${ZIP_BASE}/getting_started.pdf" ) ]]; then
    echo "ERROR: getting started guide not included"
    exit 1
  fi
  custom_verify
  echo "Zip Verified."
  cd ${verify_currdir}
}

# Generate the release
function gen {
  gen_currdir=`pwd`
  echo "Generating the release..."
  git_check

  tests=true
  set_version=false
  push=false
  new_version=''
  new_rc=''
  for i in $*; do
    case "$i" in
      -notests|-notest|-nt) tests=false;;
      -push|-p) push=true;;
      *)
        if [[ "$i" =~ ${VERSION_REGEX} ]]; then
          set_version=true
          new_version="`echo $i | sed s/-v//g`"
        elif [[ "$i" =~ ${RC_REGEX} ]]; then
          new_rc="`echo $i | sed s/-rc//g`"
        else
          echo "ERROR: invalid option: $i"
          cd "${gen_currdir}"
          usage
        fi
        ;;
    esac
  done

  if [[ ( ${set_version} = true ) && ( ${new_rc} = "" ) ]]; then
    echo "ERROR: cannot set version if rc doesn't exist"
    cd "${gen_currdir}"
    usage
  fi

  # Rev version number if requested
  if [[ ${set_version} = true ]]; then
    set_version ${new_version}
  fi

  # Run tests before doing anything to make sure we are passing.
  if [[ ${tests} = true ]]; then
    tests
  fi

  cd ${BASE_DIR}
  rm -rf ${ZIP_BASE}
  mkdir ${ZIP_BASE}

  rm -rf ${IMA_ZIP_BASE}
  mkdir ${IMA_ZIP_BASE}

  custom_gen

  #sampleapp
  cp -R ${SAMPLE_DIR} ${ZIP_BASE}/SampleApps
  cp -R ${THIRD_PARTY_SAMPLE_DIR}/IMASampleApp ${IMA_ZIP_BASE}/IMASampleApp

  #getting started guide and release notes
  cp getting_started.pdf ${ZIP_BASE}/
  cp ReleaseNotes.txt ${ZIP_BASE}/
  if [[ -f "WhyYourCodeDoesntCompile.txt" ]]; then
    cp WhyYourCodeDoesntCompile.txt ${ZIP_BASE}/
  fi

  #version file
  version=$(get_version)
  echo "v${version}" >> ${ZIP_BASE}/VERSION
  if [[ ! ( "${new_rc}" = "" ) ]]; then
    echo "RC${new_rc}" >> ${ZIP_BASE}/VERSION
  fi
  echo "Created On: ${DATE}" >> ${ZIP_BASE}/VERSION

  #docs
  doc
  cp -R ${SDK_DIR}/Documentation/public ${ZIP_BASE}/Documentation
  cp -R ${IMA_SDK_DIR}/Documentation/public ${IMA_ZIP_BASE}/Documentation

  #zip Base SDK
  cd ${BASE_DIR}
  #verify everything exists
  verify
  rm ${ZIP_NAME}
  zip -r ${ZIP_BASE} ${ZIP_BASE}/*
  rm -rf ${ZIP_BASE}

  #zip IMA SDK
  cd ${BASE_DIR}
  #verify everything exists
  #verify
  rm ${IMA_ZIP_NAME}
  zip -r ${IMA_ZIP_BASE} ${IMA_ZIP_BASE}/*
  rm -rf ${IMA_ZIP_BASE}

  echo
  echo "Release Generated!"
  if [[ ${set_version} = true ]]; then
    if [[ ${GIT_CHECK_RESULT} = false && ${push} = true ]]; then
      echo "  Not pushing because your working directory is dirty. Yuck."
    elif [[ ${push} = true ]]; then
      echo "  Pushing Gen Release v${new_version}_RC${new_rc} commit."
      git commit -a -m "Gen Release v${new_version}_RC${new_rc}"
      git tag -a v${new_version}_RC${new_rc} -m "Version ${new_version}_RC${new_rc} (Created On ${DATE})"
      git push
      git push --tags
    fi
  elif [[ ${push} = true ]]; then
    echo "  Pushing Gen Release commit."
    git commit -a -m "Gen Release"
    git push
  fi
  cd "${gen_currdir}"
}

function pub {
  pub_currdir=`pwd`
  rc=false
  push=''
  new_version=''
  new_rc=''
  for i in $*; do
    case "$i" in
      -push|-p) push=$i;;
      -candidate|-rc|-c) rc=true;;
      *) if [[ "$i" =~ ${VERSION_REGEX} ]]; then new_version="`echo $i | sed s/-v//g`"; else echo "ERROR: invalid option: $i"; cd "${pub_currdir}"; usage; fi;;
    esac
  done

  cd ${BASE_DIR}
  version="${new_version}"
  if [[ "${version}" = "" ]]; then
    version=$(get_version)
  fi

  if [[ ! ( "${version}" = "" ) && ! ( "`head -1 ReleaseNotes.txt`" =~ ${version} ) ]]; then
    echo "ERROR: Please update ReleaseNotes.txt before pushing a version"
    cd "${pub_currdir}"
    usage
  fi

  if [[ ${rc} = true ]]; then
    # Figure out which release candidate this is.
    cd "${CANDIDATE_DIR}"
    last_rc=`ls -rt | grep "${ZIP_BASE}-${version}" | tail -1 | sed "s/^${ZIP_BASE}-[0-9]*\.[0-9]*\.[0-9]*_RC\([0-9]*\)\.zip$/\1/"`
    if [[ ! ( ${last_rc} =~ ^[0-9]*$ ) ]]; then
      echo "Error: Could not figure out last release candidate"
      cd "${pub_currdir}"
      usage
    fi
    new_rc=$((last_rc+1))
    gen -v${version} -rc${new_rc} ${push}
    version_with_rc=${version}_RC${new_rc}
    if [[ "`ls \"${CANDIDATE_DIR}\" |grep ${ZIP_BASE}-`" != "" ]]; then
      echo "  Removing Existing Release Candidate"
      rm "${CANDIDATE_DIR}"${ZIP_BASE}-*
    fi
    cd ${BASE_DIR}
    echo "Publishing the Release Candidate..."
    echo "  Copying ${ZIP_NAME} to ${CANDIDATE_DIR}${ZIP_BASE}-${version_with_rc}.zip"
    cp ${ZIP_NAME} "${CANDIDATE_DIR}"${ZIP_BASE}-${version_with_rc}.zip
    echo "  Copying ${ZIP_NAME} to ${CANDIDATE_DIR}${VERSIONS_SUFFIX}${ZIP_BASE}-${version_with_rc}.zip"
    cp ${ZIP_NAME} "${CANDIDATE_DIR}"${VERSIONS_SUFFIX}${ZIP_BASE}-${version_with_rc}.zip
    echo "  Copying ${ZIP_NAME} to ${CANDIDATE_DIR}${ZIP_NAME}"
    cp ${ZIP_NAME} "${CANDIDATE_DIR}"${ZIP_NAME}
    echo "  Copying ${IMA_ZIP_NAME} to ${CANDIDATE_DIR}${IMA_ZIP_NAME}"
    cp ${IMA_ZIP_NAME} "${CANDIDATE_DIR}"${IMA_ZIP_NAME}
  else
    echo "Publishing the Release..."
    if [[ "`ls \"${RELEASE_DIR}\" |grep ${ZIP_BASE}-`" != "" ]]; then
      echo "  Removing Existing Release"
      rm "${RELEASE_DIR}"${ZIP_BASE}-*
    fi

    cd "${CANDIDATE_DIR}"
    last_rc=`ls -rt | grep "${ZIP_BASE}-${version}" | tail -1`
    if [[ ! ( ${last_rc} =~ ^${ZIP_BASE}-[0-9]*\.[0-9]*\.[0-9]*_RC[0-9]*\.zip$ ) ]]; then
      echo "Error: Could not figure out last release candidate to release"
      cd "${pub_currdir}"
      usage
    fi

    cd ${BASE_DIR}
    #add release tag
    if [[ ${push} = true ]]; then
      curr_tag=`echo "${last_rc}" | sed "s/^${ZIP_BASE}-\([0-9]*\.[0-9]*\.[0-9]*_RC[0-9]*\)\.zip$/v\1/"`
      git tag ${version} ${curr_tag}
      git push --tags
    fi

    echo "  Copying ${CANDIDATE_DIR}${last_rc} to ${RELEASE_DIR}${last_rc}"
    cp "${CANDIDATE_DIR}"${last_rc} "${RELEASE_DIR}"${last_rc}
    echo "  Copying ${CANDIDATE_DIR}${last_rc} to ${RELEASE_DIR}${VERSIONS_SUFFIX}${last_rc}"
    cp "${CANDIDATE_DIR}"${last_rc} "${RELEASE_DIR}"${VERSIONS_SUFFIX}${last_rc}
    echo "  Copying ${CANDIDATE_DIR}${last_rc} to ${RELEASE_DIR}${ZIP_NAME}"
    cp "${CANDIDATE_DIR}"${last_rc} "${RELEASE_DIR}"${ZIP_NAME}
    echo "  Copying ${CANDIDATE_DIR}${IMA_ZIP_NAME} to ${RELEASE_DIR}${IMA_ZIP_NAME}"
    cp "${CANDIDATE_DIR}"${IMA_ZIP_NAME} "${RELEASE_DIR}"${IMA_ZIP_NAME}
  fi
  cd "${pub_currdir}"
}
