#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "   DYNIMETIZE ZX - AUTOMATED APK BUILD & VERIFICATION"
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Determine Gradle runner
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
rm -rf app/build/outputs/apk/release/*

echo "[2/4] Building FRESH RELEASE APK v2.2.0 (Build 220) via $GRADLE_CMD assembleRelease..."
$GRADLE_CMD assembleRelease

RELEASE_APK="app/build/outputs/apk/release/app-release.apk"

echo "[3/4] Verifying Release APK output..."
if [ ! -f "$RELEASE_APK" ]; then
    echo "[ERROR] Release APK not found at $RELEASE_APK!"
    exit 1
fi

# Prepare output release directories with v2.2.0 artifacts
NAMED_APK="DYNIMETIZE_ZX-v2.2.0-release.apk"
mkdir -p releases release public .aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70

cp -f "$RELEASE_APK" "releases/$NAMED_APK"
cp -f "$RELEASE_APK" "releases/app-release.apk"
cp -f "$RELEASE_APK" "release/$NAMED_APK"
cp -f "$RELEASE_APK" "release/app-release.apk"
cp -f "$RELEASE_APK" "public/$NAMED_APK"
cp -f "$RELEASE_APK" "public/app-release.apk"
cp -f "$RELEASE_APK" ".aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70/$NAMED_APK"

echo "[4/4] Validation Summary:"
echo "----------------------------------------------------------"
RELEASE_SIZE=$(stat -c%s "releases/$NAMED_APK" 2>/dev/null || stat -f%z "releases/$NAMED_APK" 2>/dev/null || wc -c < "releases/$NAMED_APK")
SHA256_SUM=$(sha256sum "releases/$NAMED_APK" | awk '{print $1}')

echo "✔ RELEASE APK : releases/$NAMED_APK ($RELEASE_SIZE bytes)"
echo "✔ SHA-256     : $SHA256_SUM"
echo "----------------------------------------------------------"
echo "BUILD SUCCESSFUL! Fresh Release APK v2.2.0 is verified and published ready for download."
