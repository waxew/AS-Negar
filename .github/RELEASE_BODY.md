## What's new in 5.0.0

ReFra 5.0 is a major release headlined by full cloud media support. Your self-hosted server library now lives alongside your device photos in one unified timeline, with a powerful new backup & restore wizard, fresh editing tools, and a wave of performance work.

### New Features

- **Cloud media & Immich integration** — Connect your self-hosted Immich server and browse your remote library directly inside Gallery. Cloud assets merge into a single unified timeline with smart local/remote deduplication and per-thumbnail sync indicators. Includes email/password and API-key sign-in with encrypted credential storage, multi-server support, full-quality streaming, EXIF display, favoriting, server albums, recognized people (with name/birthday editing), map browsing, CLIP-based smart search, and shared-link management. ownCloud groundwork is also in place.
- **Cloud photo backup** — Pick which local albums to back up to your server with checksum-based deduplication to avoid re-uploads, separate Wi-Fi/cellular rules for photos and videos, background scheduling, and an upload details screen.
- **Backup & restore wizard** — Export and import your settings, favorites, cloud server configs, and vaults as a single archive, with optional password encryption. Vault backup is off by default and authenticates each selected vault with its own credential before it's included.
- **New editor effects & filters** — A new Effects tab adds Posterize, Edges, and Borders with live preview, while the Colour tab gains GPU-accelerated Hue and black & white tools and the Filters grid gets a one-tap Negative preset.
- **Flexible date grouping** — Group your media by day, month, or year, configured independently per screen (Timeline, Favorites, albums, locations, and cloud archive), with per-screen date-separator toggles.
- **Album sections** — Automatically organize albums into collapsible Common, Apps, and Other sections based on where they're stored, with manual "Move to section" overrides. Off by default.
- **Save motion photo as video** — A Google Photos-style "Save as video" action exports the embedded clip from a motion photo as a standalone MP4 in your Movies folder.
- **Android Advanced Protection** — On Android 16+, enabling device-wide Advanced Protection automatically force-enables sandboxed image decoding and stronger metadata isolation. Your preferences are preserved and restored when it's turned off (#900).
- **Location detail sheet** — Tapping a photo's location opens a bottom sheet with a larger map preview, the media thumbnail, and quick actions to open it in app or in an external map.
- **Improved APNG & JXL support** — Better animated APNG playback and JPEG XL image handling.
- **Hide favorite button** — New separate setting to hide the favorite button next to the search bar.

### Performance

- **Smoother timeline scrolling** — Vendored scrollbar module, optimized grid rendering, a new `MediaCellState` to cut per-cell recompositions, and image loading paused while fast-scrolling via the scrollbar thumb.
- **New viewer image pipeline** — Replaced Glide with a Sketch preview+full painter pattern in the media viewer and story cards, plus grid-click prefetching for flicker-free transitions.
- **Faster startup** — Prevented the ignored-albums flash on restart and optimized startup work.
- **Reduced blur lag** — Lowered media viewer bottom-sheet blur lag.

### Improvements

- Cloud media is deduplicated across search and location flows
- Calendar-day difference is now used for timeline date grouping
- Migrated build scripts to Gradle plugins and added a Google Play variant
- `VaultPasswordUnlockSheet` can show an optional subtitle naming the vault

### Bug Fixes

- Fixed file-descriptor-backed resource leaks
- Fixed horizontal mirroring of panorama images
- Fixed video continuing to play (background audio) when the app loses focus
- Fixed album content not showing in the picker for timeline-only hidden albums
- Fixed custom album thumbnails not appearing in the picker's album tab
- Fixed inconsistent status bar icon colors in the media viewers
- Fixed Settings summary text overlapping with the switch
- Fixed duplicate StoryCard keys and consecutive video playback in the story viewer
- Fixed several vault UX issues (deferred creation, confirmation dialogs, touch blocking)
- Fixed a duplicate-key crash in the viewer pager by deduplicating media by ID
- Fixed grid pinch-zoom conflicting with scrolling
- Fixed back navigation from StandaloneActivity via taskAffinity (#886)
- Fixed ContentObservers not refreshing after move/copy by notifying the Files URI
- Fixed a vault decryption temp-file leak that caused large storage bloat
- Fixed copy/move to write into SD card folders instead of internal storage
- Fixed trashing of external storage media when using MANAGE_EXTERNAL_STORAGE
