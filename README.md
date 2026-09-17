# AI Life Simulator — Android & Self-Hosted PocketBase Backend

> **Infinite Procedural Multiverse Life Simulation** with 3-layer AI architecture, OCEAN Big Five personality NPCs, Russell circumplex emotions, Ed25519-signed offline coupons, embedded NanoHTTPD admin console, and automated GitHub Actions CI.

---

## 🏛️ Architecture Overview

```mermaid
graph TD
    subgraph Android Application
        UI[Jetpack Compose UI & Screens] --> VM[MainViewModel]
        VM --> AI[AI Engine Cascade]
        VM --> PB_REPO[PocketBase Repository]
        VM --> ROOM[(Local Room DB)]
        ADMIN_SRV[Embedded NanoHTTPD Port 8080] --> ASSETS[Admin SPA Assets]
        AI --> PUTER[Headless Puter.js WebView]
        AI --> FALLBACK[OpenAI Fallback API]
    end

    subgraph PocketBase Backend (Docker)
        PB[PocketBase Server :8090]
        HOOKS[pb_hooks/main.pb.js]
        MIGRATIONS[pb_migrations]
        SQLITE[(PocketBase SQLite)]
    end

    subgraph Cloud & External APIs
        PLAY[Google Play Developer API]
        GITHUB[GitHub Actions CI/CD]
    end

    PB_REPO -->|REST & WebSocket| PB
    HOOKS -->|Receipt Validation| PLAY
    GITHUB -->|Automated Tests & APK Builds| Android Application
```

---

## 🚀 GitHub Actions CI & Keystores Setup

The project includes an enterprise-grade GitHub CI workflow in `.github/workflows/ci.yml`.

### Automated Pipeline Jobs
1. **`test`**: Restores or generates `debug.keystore`, runs Unit & Robolectric tests (`gradle :app:testDebugUnitTest`), archives JUnit test reports.
2. **`build-debug`**: Assembles debug APK (`gradle :app:assembleDebug`) and uploads artifact `app-debug.apk`.
3. **`build-release`**: Assembles signed release APK & Android App Bundle (`.aab`) and archives them.

### Setting Up Keystore Secrets in GitHub
To sign release builds in GitHub Actions, add these repository secrets (**Settings** → **Secrets and variables** → **Actions**):

| Secret Name | Description | How to Generate |
| :--- | :--- | :--- |
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded release keystore file | Run `./scripts/generate_keystore.sh release` |
| `STORE_PASSWORD` | Keystore password | Custom secure password |
| `KEY_PASSWORD` | Key alias password | Custom secure password |
| `DEBUG_KEYSTORE_BASE64` | (Optional) Custom debug keystore | Run `./scripts/generate_keystore.sh debug` |

### Keystore Helper Scripts

#### 1. Generate Keystores (`scripts/generate_keystore.sh`)
```bash
chmod +x scripts/generate_keystore.sh

# Generate a release keystore:
./scripts/generate_keystore.sh release "MyStrongPassword2026!"

# Generate a debug keystore:
./scripts/generate_keystore.sh debug
```

#### 2. Generate Ed25519 Coupon Keypair (`scripts/generate_keys.sh`)
```bash
chmod +x scripts/generate_keys.sh
./scripts/generate_keys.sh
```
Outputs `COUPON_PRIVATE_KEY` for PocketBase and `COUPON_PUBLIC_KEY` for Android.

---

## 🐳 PocketBase Self-Hosted Backend

The backend runs on a lightweight Docker container with Goja hooks and SQLite.

### Quick Start
```bash
cd pocketbase
cp .env.example .env
docker compose up -d
```

### Endpoints
- **PocketBase Admin Dashboard**: `http://localhost:8090/_/`
- **Receipt Validation**: `POST http://localhost:8090/api/validate-receipt`
- **Coupon Redemption**: `POST http://localhost:8090/api/redeem-coupon`
- **Admin Coupon Minting**: `POST http://localhost:8090/api/admin/generate-coupon`

---

## 📱 Embedded In-App Admin Console (NanoHTTPD)

- Starts an embedded HTTP server on **Port 8080** directly on the Android device.
- Accessible locally or across your LAN at `http://<device-ip>:8080`.
- Unlock in app: Navigate to **Settings**, tap **App Version 7 times**, and enter developer PIN.
