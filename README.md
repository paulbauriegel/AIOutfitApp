# AI Outfit

Native Android wardrobe inventory app for cataloging clothes, categorizing them, adding metadata, attaching photos, and combining items into outfits.

## Features

- Clothing inventory with search and category filtering.
- Editable metadata: category, color, season, size, brand, material, care instructions, notes, and photo URI.
- Category management with sensible default clothing categories.
- Outfit builder that combines selected clothing items with occasion, season, and notes.
- Local persistence through Android `SharedPreferences` using JSON, with no external runtime dependencies.

## Build

Open the project in Android Studio, or build from the command line with a local Gradle installation:

```sh
gradle test assembleDebug
```

The debug APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```
