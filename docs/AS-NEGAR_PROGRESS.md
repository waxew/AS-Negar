# AS-Negar Progress

## Completed

- Repository identified as an AS Team customization of ReFra / Gallery; upstream license and required attribution are preserved.
- Central AS-Negar brand constants added under `core/branding/ASBrand.kt`.
- Stable AS Team Android application identity migrated to `com.asteam.negar`.
- Internal Kotlin namespace intentionally remains `com.dot.gallery` during the staged migration to protect Room schemas, migrations and imports.
- Debug, release, staging and Google Play media-provider authorities migrated to the `com.asteam.negar` identity.
- Output archive naming migrated from ReFra to AS-Negar.
- Persian resource baseline created in `values-fa`.
- Shared AS Team right-side Compose drawer implemented and connected to `MainActivity` without replacing the upstream NavHost.
- Drawer profile header implemented with circular image selection and persisted URI permission.
- Drawer item order aligned with the AS Team standard: Settings index 0, Share index 1.
- Home, Settings, Share, About and Contact actions connected.
- Drawer gestures and hamburger are limited to top-level Timeline, Albums and Library routes so viewer/editor screens are not obstructed.
- About shows the application version dynamically and AS Team identity/support information.
- GitHub Actions workflow repaired so it creates real jobs instead of failing at workflow startup.
- AS-Negar identity verification passes in CI.
- `:app:compileArm64-v8aNoMLDebugKotlin` completed successfully in CI after the AS-Negar identity and drawer integration.

## Current architecture

The app uses Kotlin, Jetpack Compose, Navigation Compose, Room, Hilt and the existing ReFra gallery navigation. `MainActivity` creates the shared `NavHostController`; `ASDrawerHost` wraps the root app UI; `NavigationComp` remains responsible for gallery, viewer, editor, settings, cloud and other upstream destinations.

The public install identity is `com.asteam.negar`. The source namespace remains `com.dot.gallery` until a dedicated namespace migration is justified and can be verified against Room schemas and generated sources.

## Build status

- Kotlin/Compose compile: **verified successful**.
- Debug APK assemble: **running / not yet marked verified in this document**.
- Debug APK artifact upload: pending successful assemble.
- Release APK: pending release-signing configuration and final release verification.

## Branding status

Product-facing ReFra/Gallery strings are being migrated selectively to AS-Negar/Negar. Upstream source attribution such as the ReFra source/model repository is intentionally retained where it represents actual third-party origin rather than product branding.

## Next implementation phase

- Verify the assembled debug APK and retain the CI artifact.
- Run the current `main` build including the final drawer behavior and branding baseline.
- Replace or adapt remaining upstream support/donation UI so it cannot be confused with AS Team support while preserving license attribution.
- Verify the existing update/version infrastructure; add the AS-Negar update checker if the upstream project does not provide an appropriate one.
- Add AS-Negar launcher icon and splash assets after the build baseline is stable.
- Prepare release signing, checksum and signature-verification outputs for the final delivery pack.
