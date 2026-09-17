#!/usr/bin/env bash
# ==============================================================================
# AI LIFE SIMULATOR — ED25519 KEY GENERATION UTILITY FOR COUPON SIGNING
# Generates private and public Ed25519 keypair in PKCS#8 DER & Base64 format.
# ==============================================================================

set -e

KEY_DIR="./keys"
mkdir -p "$KEY_DIR"

echo "==> Generating Ed25519 keypair for coupon signing/verification..."

if command -v openssl >/dev/null 2>&1; then
    # Generate private key in PKCS#8 PEM
    openssl genpkey -algorithm ed25519 -out "$KEY_DIR/ed25519_private.pem"
    # Extract public key
    openssl pkey -in "$KEY_DIR/ed25519_private.pem" -pubout -out "$KEY_DIR/ed25519_public.pem"

    # Extract raw 32-byte public key and private key in base64
    PRIV_B64=$(openssl pkey -in "$KEY_DIR/ed25519_private.pem" -outform DER | base64 -w 0)
    PUB_B64=$(openssl pkey -in "$KEY_DIR/ed25519_public.pem" -pubin -outform DER | base64 -w 0)

    echo ""
    echo "=============================================================================="
    echo "ED25519 KEYS GENERATED SUCCESSFULLY"
    echo "=============================================================================="
    echo "POCKETBASE SERVER ENV VAR (COUPON_PRIVATE_KEY):"
    echo "$PRIV_B64"
    echo ""
    echo "ANDROID APP / CLIENT ENV VAR (COUPON_PUBLIC_KEY):"
    echo "$PUB_B64"
    echo "=============================================================================="

    echo "COUPON_PRIVATE_KEY=$PRIV_B64" > "$KEY_DIR/keys.env"
    echo "COUPON_PUBLIC_KEY=$PUB_B64" >> "$KEY_DIR/keys.env"
    echo "Keys saved to $KEY_DIR/keys.env"
else
    echo "OpenSSL not found. Please install openssl to generate keys."
    exit 1
fi
