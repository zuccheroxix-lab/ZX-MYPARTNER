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

echo "[1/3] Building RELEASE APK via $GRADLE_CMD assembleRelease..."
$GRADLE_CMD assembleRelease

RELEASE_APK="app/build/outputs/apk/release/app-release.apk"

echo "[2/3] Verifying Release APK output..."
if [ ! -f "$RELEASE_APK" ]; then
    echo "[ERROR] Release APK not found at $RELEASE_APK!"
    exit 1
fi

# Clean old debug APKs
rm -f releases/app-debug.apk release/app-debug.apk .aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70/app-debug.apk

# Prepare output release directories
NAMED_APK="DYNIMETIZE_ZX-v2.0.0-release.apk"
mkdir -p releases release public .aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70

cp -f "$RELEASE_APK" "releases/$NAMED_APK"
cp -f "$RELEASE_APK" "releases/app-release.apk"
cp -f "$RELEASE_APK" "public/$NAMED_APK"
cp -f "$RELEASE_APK" "public/app-release.apk"
cp -f "$RELEASE_APK" ".aistudio/artifacts/brain/6e88e88f-842a-46b6-bb5f-6a9cf9d00d70/$NAMED_APK"

echo "[3/3] Validation Summary:"
echo "----------------------------------------------------------"
RELEASE_SIZE=$(stat -c%s "releases/$NAMED_APK" 2>/dev/null || stat -f%z "releases/$NAMED_APK" 2>/dev/null || wc -c < "releases/$NAMED_APK")

echo "✔ RELEASE APK : releases/$NAMED_APK ($RELEASE_SIZE bytes)"
echo "----------------------------------------------------------"
echo "BUILD SUCCESSFUL! Release APK is verified and ready for download."
