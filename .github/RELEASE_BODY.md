## What's new in 4.2.3

### New Features

- **Timeline Filter** — Filter your timeline by media type (photos, videos, GIFs), favorites, year, or album. Access the filter directly from the search bar. Combine multiple filters for precise results.
- **Animated Format Support** — Play animated AVIF, animated JXL, and APNG images alongside traditional GIF. Modern animated formats with better compression and quality are now fully supported.
- **Video Subtitles** — Select embedded subtitle tracks or load external subtitle files (.srt, .ass, .ssa, etc.) during video playback. Manage subtitles from a new bottom sheet with track selection and delete support.
- **Video Pinch-to-Zoom** — Pinch-to-zoom and double-tap-to-zoom during video playback, just like with photos. Pan around while zoomed in to examine fine details in video footage.
- **Search Descriptions & Metadata** — Search queries now match against image descriptions, EXIF metadata text fields, and other embedded metadata — not just filenames and AI analysis.
- **Album Picker Search** — A search bar in the copy/move album picker lets you quickly find the target album by name when you have many albums.
- **Redesigned Trash & Favorites** — Both screens have been completely redesigned with a fresh layout, improved visual hierarchy, and smoother animations.

### UX Improvements

- Filter moved from separate UI to inline search bar with a settings toggle to control visibility
- Dismissible Donate/GitHub banners in Settings
- Bottom sheet confirmation replaces dismiss X button for a more consistent experience
- Optimistic UI feedback when trashing images for snappier interaction

### Bug Fixes

- Fixed drag-selection tracking not initializing correctly when no prior selection existed
- Fixed keyboard overlapping the metadata edit bottom sheet (added imePadding)
- Preserved original `DATE_TAKEN` and `DATE_ADDED` when overriding an edited photo

