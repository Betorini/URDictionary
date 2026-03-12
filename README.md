# UR Dictionary – Uranian Astrology Dictionary (v2)
## Rebuilt from original source code – Jetpack Compose + Material 3

---

## What changed from v1 (critical fixes based on original Java source)

### Database approach
The original app ships `planetary.sqlite` as a **raw resource** (`res/raw/`),
copies it to `databases/` on first launch, then opens it read-only.
This project does the same via `DatabaseHelper.kt`.

**Do NOT use CSV parsing at runtime.** The SQLite file is pre-built and bundled.

### Query logic (exact replica of `PlanetaryDAO.getAllplanetary()`)
```
userInput = "SU JU"
trim      = "su ju"          (lowercase for combine; uppercase for ruth/rutheng/symphony)
reversed  = "ju su"          (col1[3..] + " " + col1[0..2])

SQL: SELECT * FROM '{table}' WHERE aa='{trim}' OR aa='{reversed}'

Result format (combine / ruth / rutheng):
  col[1][0..2].upper + "+" + col[1][3..5].upper + "-" + col[2][0..2].upper + " " + col[2][3..]
  → "SU+JU-XX : โชคลาภ…"

Result format (symphony):
  col[1][0..2].upper + "/" + col[1][3..5].upper + " : " + col[2]
  → "SU/JU : keywords…"
```

### Selection limits (exact replica of original counter logic)
- Max **2 symbols** selected (ct counter < 2)
- Tapping a 3rd symbol while 2 are selected does nothing
- CLEAR resets all counters

### Dictionary sources (context menu → source selector chip)
| Source | Table | aa case | bg color |
|--------|-------|---------|----------|
| อ.ประยูร พลอารีย์ | `combine` | lowercase | #FFD4D8D7 |
| Ruth Brummund TH | `ruth` | UPPERCASE | #FFD4B546 |
| Ruth Brummund EN | `rutheng` | UPPERCASE | #FFD4B546 |
| Symphony of Planet | `symphony` | UPPERCASE | #99FFFF |

### Keyword search (Dicsearch)
- Splits query on spaces, up to 3 words
- AND/OR toggle switch
- `WHERE bb LIKE '%word1%' AND/OR bb LIKE '%word2%'`

---

## Setup

### 1. Database (REQUIRED)
The `res/raw/planetary.sqlite` file **is included** in this project (built from the 4 CSVs).
It will be automatically copied to the device on first launch.

### 2. Build
```bash
./gradlew assembleDebug
```
Open in Android Studio Hedgehog+ → Sync → Run.

---

## Project structure
```
app/src/main/
├── res/raw/planetary.sqlite          ← pre-built SQLite DB (3.2 MB)
└── kotlin/com/urdictionary/
    ├── MainActivity.kt
    ├── URDictionaryApp.kt
    ├── data/
    │   ├── AstroSymbol.kt            ← 22 symbols matching original ImageButtons
    │   ├── Models.kt                 ← DictionarySource, InterpretationRow, SearchMode
    │   ├── local/DatabaseHelper.kt   ← copies & opens planetary.sqlite
    │   └── repository/URDictionaryRepository.kt  ← exact query replica
    ├── di/AppModule.kt
    └── ui/
        ├── theme/Theme.kt            ← deep-space blue + gold astrology palette
        └── screen/
            ├── URDictionaryViewModel.kt
            └── URDictionaryScreen.kt  ← Main + Search + Favorites screens
```
