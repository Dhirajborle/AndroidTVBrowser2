# TV Browser

Minimal Android TV browser app, similar to "Browser for Android TV".

## Features
- Leanback home screen with a bookmark grid (D-pad navigable)
- "+ Add Bookmark" tile to save new sites (title + URL)
- Full WebView browser with address bar and Go button
- BACK button goes back in page history before exiting
- Bookmarks persisted locally (SharedPreferences)

## How to open
1. Open Android Studio → **Open** → select this `AndroidTVBrowser` folder.
2. Let Gradle sync (it will download dependencies).
3. Run on an Android TV emulator or device (`Run > app`).

## Project structure
- `MainActivity` / `MainFragment` — home screen bookmark grid (Leanback)
- `CardPresenter` — renders each bookmark as a focusable card
- `BrowserActivity` — WebView + address bar
- `AddBookmarkActivity` — simple form to add a bookmark
- `BookmarkStore` — SharedPreferences-based storage (swap for Room if needed)

## Next steps you might want
- Bookmark thumbnails (favicon or screenshot capture)
- Long-press to delete a bookmark
- History list
- Settings screen (homepage, search engine)
