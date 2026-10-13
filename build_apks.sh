#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "   DYNIMETIZE ZX - AUTOMATED APK BUILD & VERIFICATION"
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
elif [ -x "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
else
    echo "[ERROR] Neither 'gradle' nor './gradlew' is available."
    exit 1
fi

echo "[1/4] Cleaning previous build cache and old APK artifacts..."
rm -f releases/*.apk release/*.apk public/*.apk .aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70/*.apk
rm -rf app/build/outputs/apk/release/* app/build/outputs/apk/debug/*

echo "[2/4] Building FRESH Release and Debug APKs via $GRADLE_CMD assembleDebug assembleRelease..."
$GRADLE_CMD assembleDebug assembleRelease

DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
RELEASE_APK="app/build/outputs/apk/release/app-release.apk"

echo "[3/4] Verifying APK outputs..."
if [ ! -f "$DEBUG_APK" ]; then
    echo "[ERROR] Debug APK not found at $DEBUG_APK!"
    exit 1
fi
if [ ! -f "$RELEASE_APK" ]; then
    echo "[ERROR] Release APK not found at $RELEASE_APK!"
    exit 1
fi

RELEASE_NAMED_APK="DYNIMETIZE_ZX-v2.2.0-release.apk"
DEBUG_NAMED_APK="DYNIMETIZE_ZX-v2.2.0-debug.apk"

mkdir -p releases release public .aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70

# Copy Release artifacts
cp -f "$RELEASE_APK" "releases/$RELEASE_NAMED_APK"
cp -f "$RELEASE_APK" "releases/app-release.apk"
cp -f "$RELEASE_APK" "release/$RELEASE_NAMED_APK"
cp -f "$RELEASE_APK" "release/app-release.apk"
cp -f "$RELEASE_APK" "public/$RELEASE_NAMED_APK"
cp -f "$RELEASE_APK" "public/app-release.apk"
cp -f "$RELEASE_APK" ".aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70/$RELEASE_NAMED_APK"

# Copy Debug artifacts
cp -f "$DEBUG_APK" "releases/$DEBUG_NAMED_APK"
cp -f "$DEBUG_APK" "releases/app-debug.apk"
cp -f "$DEBUG_APK" "release/$DEBUG_NAMED_APK"
cp -f "$DEBUG_APK" "release/app-debug.apk"
cp -f "$DEBUG_APK" "public/$DEBUG_NAMED_APK"
cp -f "$DEBUG_APK" "public/app-debug.apk"

echo "[4/4] Validation Summary:"
echo "----------------------------------------------------------"
RELEASE_SIZE=$(stat -c%s "releases/$RELEASE_NAMED_APK" 2>/dev/null || wc -c < "releases/$RELEASE_NAMED_APK")
DEBUG_SIZE=$(stat -c%s "releases/$DEBUG_NAMED_APK" 2>/dev/null || wc -c < "releases/$DEBUG_NAMED_APK")
REL_SHA256=$(sha256sum "releases/$RELEASE_NAMED_APK" | awk '{print $1}')
DBG_SHA256=$(sha256sum "releases/$DEBUG_NAMED_APK" | awk '{print $1}')

echo "✔ RELEASE APK : releases/$RELEASE_NAMED_APK ($RELEASE_SIZE bytes)"
echo "✔ SHA-256     : $REL_SHA256"
echo "✔ DEBUG APK   : releases/$DEBUG_NAMED_APK ($DEBUG_SIZE bytes)"
echo "✔ SHA-256     : $DBG_SHA256"
echo "----------------------------------------------------------"
echo "BUILD SUCCESSFUL! Both APKs verified and ready."
