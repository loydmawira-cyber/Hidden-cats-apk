# Signing the release AAB

1. Add the 3 repo secrets (KEYSTORE_BASE64, STORE_PASSWORD, KEY_PASSWORD) in
   GitHub > Settings > Secrets and variables > Actions.
2. Actions tab > "Android Release Build" > Run workflow > `both` (default; or `aab` / `apk`).
3. Open the finished run > Artifacts: download `signed-release-aab` (upload this to Play Console) and `signed-release-apk` (for installing/testing on a phone). Artifacts are kept 30 days.
4. First upload: enrol in **Play App Signing** (default). The key above is only the *upload* key,
   so if it is ever lost Google can reset it.

Before every new Play upload bump `versionCode` in `app/build.gradle.kts`.
Never commit the .jks file (it is already git-ignored).
