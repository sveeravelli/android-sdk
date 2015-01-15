SDK_DIR=$(shell pwd)
GOPATH=$(shell pwd)/submodules/mobile_sdk_build_packaging_scripts
SUBMODULE_BUILD_PATH=submodules/mobile_sdk_build_packaging_scripts/src/mobile.ooyala.com/build
SUBMODULE_DEPLOY_PATH=submodules/mobile_sdk_build_packaging_scripts/src/mobile.ooyala.com/deploy

GIT_SHA=$(shell git -C $(GOPATH) rev-parse HEAD)
GIT_DIRTY=$(shell git -C $(GOPATH) diff-index --quiet HEAD -- ; echo $$?)
INSTALL_FLAGS=-ldflags "-X mobile.ooyala.com/build/common/git.BuildRepoGitSHA $(GIT_SHA) -X mobile.ooyala.com/build/common/git.BuildRepoGitDirty $(GIT_DIRTY)"

install:
	export GOPATH=$(GOPATH) && \
	cd $(SUBMODULE_BUILD_PATH)/android_test && go build $(INSTALL_FLAGS) -o $(SDK_DIR)/script/android_test && go clean
	export GOPATH=$(GOPATH) && \
	cd $(SUBMODULE_BUILD_PATH)/android_build && go build $(INSTALL_FLAGS) -o $(SDK_DIR)/script/android_build && go clean
	export GOPATH=$(GOPATH) && \
	cd $(SUBMODULE_BUILD_PATH)/android_publish_rc && go build $(INSTALL_FLAGS) -o $(SDK_DIR)/script/android_publish_rc && go clean
	export GOPATH=$(GOPATH) && \
	cd $(SUBMODULE_BUILD_PATH)/android_publish_release && go build $(INSTALL_FLAGS) -o $(SDK_DIR)/script/android_publish_release && go clean
	export GOPATH=$(GOPATH) && \
	cd $(SUBMODULE_DEPLOY_PATH)/android_deploy && go build $(INSTALL_FLAGS) -o $(SDK_DIR)/deploy/android_deploy && go clean

all: update-submodules install

update-submodules:
	git submodule init
	git submodule update

temp-update-sdk:
	echo "sdk.dir=${ANDROID_SDKS}" > ./sdk/local.properties

