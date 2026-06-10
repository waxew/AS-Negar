# PQC Hybrid APK Signing — Plan / Scaffold (Issue #901)

> Status: **planning only — not yet wired into releases.** Enabling this prematurely
> can break the GitHub release pipeline and, worse, lock out existing users on
> update. Follow the rollout checklist before turning anything on.

## Goal
Sign GitHub-released APKs with a **hybrid** identity: a classical key (RSA/EC)
**paired** with a post-quantum **ML-DSA** key, using the new APK Signature Scheme
introduced in Android 17. One APK works everywhere:

- Android 17+ verifies the PQC (ML-DSA) signature block.
- Older Android versions verify the classical v2/v3 signature and ignore the PQC block.

Reference: https://developer.android.com/about/versions/17/features#pqc-apk-signing

## Critical constraint (read first)
Google's guidance for **self-managed keys** is that you rotate to a hybrid identity
by combining a PQC key with a **new classical key — you cannot reuse the existing
classical key.** This means:

1. This is a **key rotation**, performed with `apksigner rotate` + a
   `SigningCertificateLineage` file.
2. The lineage proves `old key -> new key`, so devices that already have the app
   installed (signed with the current `release_key.jks`) will trust updates signed
   with the new key. **The lineage MUST be shipped/used on every future signing.**
3. Google Play path is unaffected — Play App Signing will offer its own PQC upgrade;
   do **not** apply self-managed rotation to the `gplay` bundle path.

## Current state (what exists today)
- Signing config: `app/build.gradle.kts` -> `signingConfigs { create("release") { storeFile = file("release_key.jks") ... } }` (env: `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`).
- GitHub release: `.github/workflows/stable.yml` builds `offline`/`maps`/`withml` APK variants and uploads via `softprops/action-gh-release`. Keystore decoded from `secrets.SIGNING_KEY` (base64) into `app/release_key.jks`.
- AGP performs v1/v2/v3 signing inline during `assemble...Release`.

## Approach: post-build re-sign step in CI
AGP does not yet expose PQC/ML-DSA signing. The pragmatic path is to let Gradle
produce **unsigned (or classically-signed) APKs**, then re-sign with the
Android 17 `apksigner` in a dedicated CI step using the new key + lineage.

### One-time key setup (local, secure machine)
```bash
# 1. New classical key (RSA 4096). You CANNOT reuse the old one.
keytool -genkeypair -v -keystore new_release.jks -alias refra-new \
  -keyalg RSA -keysize 4096 -validity 10000

# 2. ML-DSA (PQC) key — requires Android 17 build-tools apksigner / keytool support.
#    Exact CLI flags are still stabilizing; confirm against the build-tools shipped
#    with the Android 17 SDK before relying on them.
#    (Placeholder — verify flag names: -keyalg ML-DSA / ML-DSA-65)

# 3. Build the rotation lineage proving old -> new.
apksigner rotate --out refra.lineage \
  --old-signer --ks release_key.jks --ks-key-alias <OLD_ALIAS> \
  --new-signer --ks new_release.jks --ks-key-alias refra-new
```
Store as new GitHub secrets (base64): `SIGNING_KEY_NEW`, `SIGNING_LINEAGE`, plus
`ALIAS_NEW`, `KEY_PASSWORD_NEW`, `KEY_STORE_PASSWORD_NEW`.

### CI re-sign step (add to each APK build job in `stable.yml`, after the build)
```yaml
      - name: Set up Android 17 build-tools
        run: |
          sdkmanager "build-tools;37.0.0"   # confirm exact version with PQC support
          echo "APKSIGNER=$ANDROID_HOME/build-tools/37.0.0/apksigner" >> $GITHUB_ENV

      - name: Decode new keystore + lineage
        run: |
          echo "${{ secrets.SIGNING_KEY_NEW }}" | base64 -d > new_release.jks
          echo "${{ secrets.SIGNING_LINEAGE }}" | base64 -d > refra.lineage

      - name: Hybrid (classical + ML-DSA) re-sign
        run: |
          for apk in $(find app/build/outputs/apk-renamed -name "*.apk"); do
            "$APKSIGNER" sign \
              --ks new_release.jks --ks-key-alias "${{ secrets.ALIAS_NEW }}" \
              --lineage refra.lineage \
              --ks-pass pass:"${{ secrets.KEY_STORE_PASSWORD_NEW }}" \
              --key-pass pass:"${{ secrets.KEY_PASSWORD_NEW }}" \
              # NOTE: add the PQC/ML-DSA signer flags here once confirmed in the
              #       Android 17 apksigner (e.g. a second --next-signer with the
              #       ML-DSA key, or the hybrid flag the final tooling exposes).
              "$apk"
            "$APKSIGNER" verify --verbose "$apk"
          done
```

> The exact `apksigner` invocation for pairing an ML-DSA key is **not finalized in
> public tooling yet**. The block above is a scaffold: keep classical+lineage
> working first, then add the ML-DSA signer flag once the SDK ships stable support.

## Rollout checklist
- [ ] Confirm Android 17 SDK `build-tools` version that supports ML-DSA in `apksigner`.
- [ ] Generate new classical key + ML-DSA key on a secure, offline-ish machine.
- [ ] Generate and **back up** the `refra.lineage` file (losing it breaks updates).
- [ ] Add new secrets to GitHub (do not delete the old `SIGNING_KEY` until verified).
- [ ] Add the re-sign + `apksigner verify` step to `offline`/`maps`/`withml` jobs only.
- [ ] Leave `build_gplay` untouched (Play handles its own PQC upgrade).
- [ ] Test an **update install** from a current-release APK to a new hybrid APK on a
      pre-17 device (verifies lineage) and an Android 17 device (verifies PQC block).
- [ ] Publish the new signing cert SHA-256 fingerprints in release notes.

## Out of scope / risks
- Reusing the existing classical key for the hybrid identity (not allowed by Google).
- Applying rotation to the Play AAB.
- Shipping before confirming `apksigner` ML-DSA flag stability — track the issue and
  keep this behind the checklist until the tooling is GA.
