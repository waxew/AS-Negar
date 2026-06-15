## What's new in 5.0.2

ReFra 5.0.2 is a focused polish release that follows up 5.0.1 with fixes across the media viewer, photo editor, and cloud backup. Swipe and gesture handling in the viewer is more reliable, the editor's markup and crop tools behave correctly, and manual cloud backups now run regardless of the Auto-sync setting.

### Bug Fixes

- **Editor markup & crop** — Markup text now stays clamped within the canvas after drag, resize, and two-finger transforms, the crop box can be pinch-resized from its handles/edges, and resizing no longer auto-zooms the image to fit the selection (#936)
- **Editor scrubber** — Adjustments (e.g. Borders) now commit the value you scrub to instead of retaining the previously applied value when switching tools (#934)
- **Swipe-to-dismiss** — The viewer now dismisses once a swipe-down passes a natural threshold instead of requiring the full drag distance, with the haptic moved to that release point (#944)
- **Double swipe-down** — A second swipe-down during the dismiss transition no longer pops past the gallery and closes the app (#942)
- **Rotate button** — Rotation is normalised mod 360 so the rotate pill hides correctly and the rotate action works after multiple full turns (#950)
- **Top app bar over info sheet** — The viewer's back/info buttons now win hit-testing over the info sheet so they always respond (#943)
- **Quick action bar** — The faded-out action bar is no longer hit-testable while the info panel is expanded, so taps fall through to the sheet instead of triggering hidden buttons (#945)
- **Grouped video** — Fixed the member carousel overlapping the video controls and the video stretching when switching members in a similar-video group (#941)
- **Manual cloud backup** — 'Start backup' / 'Upload now' now run even when the Auto-sync toggle is off, so new photos upload to Immich on demand (#951)
- **View all metadata** — The metadata view now opens for photos launched from an external VIEW intent by URL-encoding the route (#946)

### Internal

- Switched JPEG 2000 decoding to a Maven-hosted JP2ForAndroid fork.
