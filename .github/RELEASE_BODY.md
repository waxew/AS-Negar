## What's new in 4.2.2

### Security Hardening

- **App Sandbox Protection** — `targetSandboxVersion=2` enforces stricter process isolation at the OS level. All file processing is gated through configurable sandbox modes.
- **Sandboxed Image Decoding** — HEIF, AVIF, and JXL images are decoded in isolated system processes via `SharedMemory`. If a malicious file exploits a decoder vulnerability, the damage is contained in the sandbox and cannot reach the main app.
- **Isolated Metadata Parsing** — EXIF and metadata extraction runs in per-file sandbox processes. Three modes available: Shared (default, single reused process), Hybrid (per-file for opened files, shared for batch), and Per-file (true per-file isolation for everything).
- **Private Folder** — Store sensitive files outside MediaStore using Android's Storage Access Framework (SAF). Files are hidden from the normal timeline, not indexed by other apps, and protected behind the app's security gate.
- **Encrypted Storage** — Opt-in encryption for app preferences (AES-256-GCM with Android Keystore) and Room database (SQLCipher). Protects trash index, categories, and internal data at rest — even on rooted devices.

### New Features

- **Persistent Rescan Tracking** — MediaStore rescan state is now persisted in the Room database, surviving app restarts and ensuring consistent media synchronization.
- **Various Performance Improvements** — Lazy per-album timeline flows, reduced recompositions in media viewer bottom sheet, optimized collection types and state management.

### Bug Fixes & Improvements

- Fixed extremely slow AI classification by skipping NNAPI for quantized CLIP models
- Fixed status bar icons always dark in media view when auto-contrast is off
- Handle missing `ACCESS_NETWORK_STATE` permission gracefully instead of crashing
- Fixed crash when launching map intent on devices without map apps
- Fixed AI features not showing on nomaps-withML builds that lack INTERNET permission
- Use filename-parsed date as fallback when MediaStore `DATE_TAKEN` is null
- Fixed motion player not released when swiping to non-motion photo
- Fixed crash on large batch copy operations
- Corrected auto-contrast help tip navigation
- Updated dependencies

