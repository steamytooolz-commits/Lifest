#!/usr/bin/env bash
# ==============================================================================
# AI LIFE SIMULATOR — ANDROID KEYSTORE GENERATION UTILITY
# Generates release or debug keystores and outputs base64 strings for GitHub CI.
# ==============================================================================

set -e

KEYSTORE_DIR="./keystores"
mkdir -p "$KEYSTORE_DIR"

MODE="${1:-release}" # "release" or "debug"

if [ "$MODE" = "debug" ]; then
    KEYSTORE_FILE="$KEYSTORE_DIR/debug.keystore"
    STORE_PASS="android"
    KEY_PASS="android"
    ALIAS="androiddebugkey"
    DNAME="CN=Android Debug,O=Android,C=US"

    echo "==> Generating Android Debug Keystore at $KEYSTORE_FILE..."
    rm -f "$KEYSTORE_FILE"
    keytool -genkey -v \
        -keystore "$KEYSTORE_FILE" \
        -storepass "$STORE_PASS" \
        -alias "$ALIAS" \
        -keypass "$KEY_PASS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "$DNAME"

    echo ""
    echo "=============================================================================="
    echo "DEBUG KEYSTORE GENERATED SUCCESSFULLY"
    echo "Store:    $KEYSTORE_FILE"
    echo "Alias:    $ALIAS"
    echo "Password: $STORE_PASS"
    echo "=============================================================================="
    echo "Base64 for GitHub Secret (DEBUG_KEYSTORE_BASE64):"
    base64 -w 0 "$KEYSTORE_FILE"
    echo ""

elif [ "$MODE" = "release" ]; then
    KEYSTORE_FILE="$KEYSTORE_DIR/my-upload-key.jks"
    STORE_PASS="${2:-ChangeMe2026!Secure}"
    KEY_PASS="${3:-$STORE_PASS}"
    ALIAS="upload"
    DNAME="CN=AI Life Simulator,OU=Mobile,O=AI Studio,C=US"

    echo "==> Generating Android Release Keystore at $KEYSTORE_FILE..."
    rm -f "$KEYSTORE_FILE"
    keytool -genkey -v \
        -keystore "$KEYSTORE_FILE" \
        -storepass "$STORE_PASS" \
        -alias "$ALIAS" \
        -keypass "$KEY_PASS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "$DNAME"

    echo ""
    echo "=============================================================================="
    echo "RELEASE KEYSTORE GENERATED SUCCESSFULLY"
    echo "File:     $KEYSTORE_FILE"
    echo "Alias:    $ALIAS"
    echo "Password: $STORE_PASS"
    echo "=============================================================================="
    echo "Base64 for GitHub Secret (RELEASE_KEYSTORE_BASE64):"
    base64 -w 0 "$KEYSTORE_FILE" > "$KEYSTORE_DIR/my-upload-key.base64"
    cat "$KEYSTORE_DIR/my-upload-key.base64"
    echo ""
    echo ""
    echo "Add the following secrets to GitHub Repository Settings -> Secrets and variables -> Actions:"
    echo "1. RELEASE_KEYSTORE_BASE64 -> (Content of $KEYSTORE_DIR/my-upload-key.base64)"
    echo "2. STORE_PASSWORD          -> $STORE_PASS"
    echo "3. KEY_PASSWORD            -> $KEY_PASS"
else
    echo "Usage: ./scripts/generate_keystore.sh [release|debug] [store_password] [key_password]"
    exit 1
fi
