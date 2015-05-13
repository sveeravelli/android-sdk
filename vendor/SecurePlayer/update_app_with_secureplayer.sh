#!/bin/bash
set -e #fail on any method error

function print_help {
  fname=`basename $0`
  echo "This script will take the SecurePlayer files from this package and copy them to the correct places in a correctly formatted application"
  echo ""
  echo "usage: ${fname} <path>" 
  echo ""
  echo "where <path> is the path to your application's root directory"
  echo "The script assumes that your application source code is in '<path>/app/'"
}

function check_if_folder_exists {
  path=$1
  explanation=$2
  if [ ! -d "$path" ]; then
    echo "${path} does not exist. $2"
    exit 1
  fi
}

function ensure_folder_exists {
  path=$1
  if [ ! -d "$path" ]; then
    echo "Creating path ${path}"
    mkdir $path
  fi
}

app_root_dir=$1
if [  -z "$app_root_dir" ]; then
  echo "Please specify a path to the target application"
  print_help
  exit 1
fi
check_if_folder_exists $app_root_dir "The app path you specified doesn't exist"

echo "Copying VisualOn files from this directory to Application in ${app_root_dir}"
echo ""
cur_dir=`pwd`

libs_dir="${cur_dir}/libs"
check_if_folder_exists $libs_dir "Are you running this script in the correct folder?"

assets_dir="${cur_dir}/assets"
check_if_folder_exists $assets_dir "Are you running this script in the correct folder?"

app_libs_dir="${app_root_dir}/app/libs"
ensure_folder_exists $app_libs_dir

app_jni_dir="${app_root_dir}/app/src/main/jniLibs"
ensure_folder_exists $app_jni_dir

app_assets_dir="${app_root_dir}/app/src/main/assets"
ensure_folder_exists $app_assets_dir

echo "copying jars from ${libs_dir} to ${app_libs_dir}"
cp ${libs_dir}/*.jar ${app_libs_dir}

echo "copying x86 shared objects from ${libs_dir} to ${app_jni_dir}"
cp -r ${libs_dir}/x86 ${app_jni_dir}

echo "copying armeabi shared objects from ${libs_dir} to ${app_jni_dir}"
cp -r ${libs_dir}/armeabi ${app_jni_dir}

echo "copying armeabi-v7a shared objects from ${libs_dir} to ${app_jni_dir}"
cp -r ${libs_dir}/armeabi-v7a ${app_jni_dir}

echo "copying assets from ${assets_dir} to ${app_assets_dir}"
cp ${assets_dir}/* ${app_assets_dir}

