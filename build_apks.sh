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

echo "[1/4] Building DEBUG APK via $GRADLE_CMD assembleDebug..."
$GRADLE_CMD assembleDebug

echo "[2/4] Building RELEASE APK via $GRADLE_CMD assembleRelease..."
$GRADLE_CMD assembleRelease

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

# Prepare output release directory
mkdir -p release
cp "$DEBUG_APK" release/app-debug.apk
cp "$RELEASE_APK" release/app-release.apk

echo "[4/4] Validation Summary:"
echo "----------------------------------------------------------"
DEBUG_SIZE=$(stat -c%s "release/app-debug.apk" 2>/dev/null || stat -f%z "release/app-debug.apk" 2>/dev/null || wc -c < "release/app-debug.apk")
RELEASE_SIZE=$(stat -c%s "release/app-release.apk" 2>/dev/null || stat -f%z "release/app-release.apk" 2>/dev/null || wc -c < "release/app-release.apk")

echo "✔ DEBUG APK   : release/app-debug.apk   ($DEBUG_SIZE bytes)"
echo "✔ RELEASE APK : release/app-release.apk ($RELEASE_SIZE bytes)"
echo "----------------------------------------------------------"
echo "BUILD SUCCESSFUL! Both APKs are verified and GitHub ready."
