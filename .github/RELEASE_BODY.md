## What's new in 5.0.3

ReFra 5.0.3 is a stability release that sharpens the media viewer and photo editor. Rotation, insets and the info sheet now behave correctly, timeline scroll position is preserved when you return from the viewer, video metadata is far richer, and the editor's markup and crop tools have been ironed out.

### New Features

- **Cleaner metadata UI** — The "View all metadata" row is now hidden when an item has no readable metadata, so you never open an empty metadata screen

### Improvements

- **Richer video metadata** — iPhone HEVC `.MOV` files now surface full QuickTime/EXIF details (Make, Model, lens, focal length, dates) instead of an empty sheet (#976)
- **Faster private folder loading** — Media now fills in progressively as it is discovered instead of waiting on a long empty-state delay while the whole folder is scanned (#968)
- **Fix black video on hide** — A new Media Viewer › Video Playback toggle re-binds the player surface when system bars are shown/hidden, fixing video blackouts on some devices (on by default for Samsung) (#967)

### Bug Fixes

- **Rotation & insets** — Media view paddings, the info bottom sheet and video controls now adapt correctly on orientation change, and the floating nav bar reserves the right bottom inset when nav sits on the sides (#929)
- **Timeline scroll position** — Preserved when returning from or closing the viewer after swiping between photos (#960, #965)
- **Info panel** — No longer auto-hides mid-drag and now dismisses cleanly instead of getting stuck invisible but interactive (#964)
- **Material navigation toggle** — Switching "Use material navigation" no longer blinks and recomposes the whole screen (#973)
- **Vault hide** — Fixed a silent failure when hiding items from the selection sheet while the media list changed (#970)
- **View all metadata from intent** — Now works for images opened from another app via the standalone viewer (#946, #959)
- **Pull-to-refresh** — Clears a refresh spinner that could get stuck forever when navigating away mid-refresh (#958)
- **Editor crop** — No longer zooms in when the crop box is dragged out of bounds (#956)
- **Editor markup & adjustments** — Fixed a markup hang on the back gesture, tool icons un-highlighting at default values, no-op adjustments stacking on undo, the rotate button persisting through a cancelled swipe, and markup text drifting off-canvas when it contains blank lines (#955, #957, #961, #962, #963, #978)
- **Secure camera** — Camera reviews launched from the lockscreen now open correctly over the keyguard (#878)
- **Wording** — Uses "Overwrite" instead of "Override" when replacing files (#975)
