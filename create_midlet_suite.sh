#!/bin/sh
RESOURCES="resources"

echo -n "Enter project name [MyProject]: "
read -e PROJECT_NAME 
if [ -z "${PROJECT_NAME}" ]; then
	PROJECT_NAME="MyProject"
fi

echo -n "Enter project description [My MIDlet Suite]: "
read -e PROJECT_DESC
if [ -z "${PROJECT_DESC}" ]; then
	PROJECT_DESC="My MIDlet Suite"
fi

echo -n "Enter project version [1.0.0]: "
read -e PROJECT_VERSION
if [ -z "${PROJECT_VERSION}" ]; then
        PROJECT_VERSION="1.0.0"
fi

echo -n "Enter target project location [`pwd`/${PROJECT_NAME}]: "
read -e PROJECT_LOC
if [ -z "${PROJECT_LOC}" ]; then
	PROJECT_LOC=`pwd`/"${PROJECT_NAME}"
fi

echo -n "Enter MIDlet Suite vendor [MIDlet Suite Vendor]: "
read -e MIDLET_VENDOR
if [ -z "${MIDLET_VENDOR}" ]; then
	MIDLET_VENDOR="MIDlet Suite Vendor"
fi

echo -n "Enter MIDlet [1] Name [MyMIDlet]: "
read -e MIDLET_NAME
if [ -z "${MIDLET_NAME}" ]; then
	MIDLET_NAME="MyMIDlet"
fi

echo -n "Enter MIDlet [1] Package []: "
read -e MIDLET_PKG

echo -n "Enter MIDlet Suite Configuration [CLDC-1.1]: "
read -e MIDLET_CONFIG
if [ -z "${MIDLET_CONFIG}" ]; then
	MIDLET_CONFIG="CLDC-1.1"
fi

echo -n "Enter MIDlet Suite Profile [MIDP-2.0]: "
read -e MIDLET_PROFILE
if [ -z "${MIDLET_PROFILE}" ]; then
	MIDLET_PROFILE="MIDP-2.0"
fi

SUITE_ICON="suite_icon.png"
MIDLET_PKG_DIR=`echo ${MIDLET_PKG} | sed 's/\./\//g'`

if [ -z "$MIDLET_PKG" ]; then
	MIDLET_CLASS="${MIDLET_NAME}"
else
	MIDLET_CLASS="${MIDLET_PKG}.${MIDLET_NAME}"
fi

mkdir -p "${PROJECT_LOC}/src/${MIDLET_PKG_DIR}"/
mkdir -p "${PROJECT_LOC}/res/"
mkdir -p "${PROJECT_LOC}/lib/"
mkdir -p "${PROJECT_LOC}/tools/"

cp "${RESOURCES}/build.xml" "${PROJECT_LOC}/"
cp "${RESOURCES}/Application Descriptor" "${PROJECT_LOC}/"
cp "${RESOURCES}/lib.properties" "${PROJECT_LOC}/lib"
cp "${RESOURCES}/sign.properties.default" "${PROJECT_LOC}/"
cp "${RESOURCES}/suite_icon.png" "${PROJECT_LOC}/res"
cp "${RESOURCES}/MIDlet.java" \
	"${PROJECT_LOC}/src/${MIDLET_PKG_DIR}/${MIDLET_NAME}.java"
cp "${RESOURCES}/download-libs.sh" "${PROJECT_LOC}/tools/"

cd "${PROJECT_LOC}"
sed -i "s/PROJECT_NAME/${PROJECT_NAME}/g" build.xml
sed -i "s/PROJECT_DESC/${PROJECT_DESC}/g" build.xml
sed -i "s/PROJECT_VERSION/${PROJECT_VERSION}/g" build.xml

sed -i "s/PROJECT_NAME/${PROJECT_NAME}/g" "Application Descriptor"
sed -i "s/PROJECT_VERSION/${PROJECT_VERSION}/g" "Application Descriptor"
sed -i "s/SUITE_VENDOR/${MIDLET_VENDOR}/g" "Application Descriptor"
sed -i "s/MIDLET_NAME/${MIDLET_NAME}/g" "Application Descriptor"
sed -i "s/SUITE_ICON/${SUITE_ICON}/g" "Application Descriptor"
sed -i "s/MIDLET_CLASS/${MIDLET_CLASS}/g" "Application Descriptor"
sed -i "s/SUITE_CONFIG/${MIDLET_CONFIG}/g" "Application Descriptor"
sed -i "s/SUITE_PROFILE/${MIDLET_PROFILE}/g" "Application Descriptor"

if [ -z "${MIDLET_PKG}" ]; then
	sed -i "s/MIDLET_PKG//g" "src/${MIDLET_PKG_DIR}/${MIDLET_NAME}.java"
else 
	sed -i "s/MIDLET_PKG/package ${MIDLET_PKG};/g" \
		"src/${MIDLET_PKG_DIR}/${MIDLET_NAME}.java"
fi
sed -i "s/MIDLET_NAME/${MIDLET_NAME}/g" \
	"src/${MIDLET_PKG_DIR}/${MIDLET_NAME}.java"

echo -n "Would you like to download the required libraries now [y|n]? "
read -e DOWNLOAD
echo
if [ "${DOWNLOAD}" = "y" ]; then
	echo "Downloading required libraries..."
	sh tools/download-libs.sh
fi
echo "Done."

