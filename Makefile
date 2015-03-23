MAKEFILE_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
EXES = android_test android_build android_publish_rc android_publish_release android_deploy android_update_candidate_sample_app_rc android_app_clean_sample_apps android_app_copy_from_vendor android_app_get_latest_rc android_app_get_latest_release android_app_merge_into_complete_sample_app android_app_update_from_target_location
SCRIPT_SUBMODULE_DIR = $(MAKEFILE_DIR)/submodules/mobile_sdk_build_packaging_scripts
SCRIPT_SUBMODULE_BIN_DIR = $(SCRIPT_SUBMODULE_DIR)/bin
SCRIPT_SDK_BIN_DIR = $(MAKEFILE_DIR)/script

generate-build-tools:
	@pushd $(SCRIPT_SUBMODULE_DIR); make test && make install
	@for me in $(EXES); do \
		cp $(SCRIPT_SUBMODULE_BIN_DIR)/$$me $(SCRIPT_SDK_BIN_DIR)/$$me; \
		if ! [ -e $(SCRIPT_SDK_BIN_DIR)/$$me ] ; then echo missing $(SCRIPT_SDK_BIN_DIR)/$$me; exit 1; fi; \
	done

update-submodules:
	git submodule init
	git submodule update

all: update-submodules generate-build-tools
