# Find the Cat: Google Play Release Checklist

This release checklist guides the developer through publishing **Find the Cat** to Google Play.

---

## 1. App Details & Branding
- [ ] **App Title:** "Find the Cat" (under 30 characters, no forbidden promotional keywords).
- [ ] **Short Description:** "A cozy hidden-object puzzle game searching for sneaky cats." (under 80 characters).
- [ ] **Full Description:** Describes all 12 initial levels, daily challenges, cozy themes, hint system, and offline capabilities.
- [ ] **App Icon:** 512x512 high-resolution icon matching the adaptive launcher icon.
- [ ] **Feature Graphic:** 1024x500 cozy banner featuring the orange cat mascot.
- [ ] **Screenshots:** At least 4 portrait screenshots (1080x1920 or higher) for phones and 7-inch/10-inch tablets showcasing:
  1. Cozy Living Room & Baker's Kitchen gameplay
  2. Pan & zoom smooth navigation
  3. Celebratory victory star rating & rewards
  4. Daily challenge & Community Leaderboard

---

## 2. Privacy Policy & Data Safety
- [ ] **Privacy Policy URL:** Host a clear privacy policy stating that:
  - No personal data, contacts, location, camera, or audio are collected.
  - Anonymous sign-in or local storage is used for progress and daily challenges.
  - Purchases are processed directly by Google Play.
- [ ] **Data Safety Form in Play Console:**
  - *Data Collection:* No user personal identifiers collected.
  - *Data Sharing:* None.
  - *Security Practices:* Encrypted in transit (HTTPS), user can request deletion (Reset Progress feature in Settings).

---

## 3. In-App Products Setup (Google Play Billing)
In the Google Play Console under **Monetize > In-app products**, create the 4 configured items (IDs must match `BillingProduct.kt` exactly):
1. `hint_pack_small`
   - Type: Consumable
   - Name: Pouch of 5 Hints
   - Price: $0.99
2. `hint_pack_large`
   - Type: Consumable
   - Name: Basket of 20 Hints (Best Value)
   - Price: $2.49
3. `remove_ads`
   - Type: Non-consumable
   - Name: Remove Ads
   - Price: $1.99
4. `premium_upgrade`
   - Type: Non-consumable
   - Name: Master Detective Pack (8x8 & 9x9 boards, unlimited hints, no ads)
   - Price: $4.99

- [ ] Add License Testers under **Setup > License Testing** with `RESPOND_NORMALLY` to verify purchase states without real charges.

---

## 4. Firebase & Cloud Firestore
- [ ] Create a project in Firebase Console.
- [ ] Add the Android App with package name `com.aistudio.findthecat.puzl` (or your final package name).
- [ ] Download `google-services.json` and place it in the `app/` directory (or configure `GOOGLE_SERVICES_JSON_BASE64` in GitHub Secrets).
- [ ] Deploy security rules using Firebase CLI:
  ```bash
  firebase deploy --only firestore:rules
  ```
- [ ] Verify that offline mode continues to work smoothly even when network is disconnected.

---

## 5. App Signing & Production Build
- [ ] Generate your upload keystore:
  ```bash
  keytool -genkey -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
  ```
- [ ] Configure GitHub Secrets for automated CI/CD builds:
  - `ANDROID_KEYSTORE_BASE64`
  - `ANDROID_KEYSTORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`
  - `GOOGLE_SERVICES_JSON_BASE64`
- [ ] Build release Android App Bundle locally:
  ```bash
  gradle :app:bundleRelease
  ```
- [ ] Upload generated `.aab` to Play Console Internal Testing track.
