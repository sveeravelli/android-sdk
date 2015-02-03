MAKEFILE_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
EXES = android_test android_build android_publish_rc android_publish_release android_deploy
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
