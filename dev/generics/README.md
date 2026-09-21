# Bulk-fill Generics from the laptop

"Generics" are user-created foods (`Product` rows with `source = User`, manual nutriments,
no OpenFoodFacts/USDA lookup). Typing them one at a time on the phone's keyboard is slow —
this folder bootstraps them from a CSV on your laptop instead, using the app's **existing**
CSV product importer (no app code involved).

## Workflow

1. Edit `seed-generics.csv`, or append one row with:
   ```bash
   python3 dev/generics/generics.py add \
     --file dev/generics/seed-generics.csv \
     --name "Poulet rôti" --kcal 190 --protein 27 --carbs 0 --fat 9 --note "roasted, per 100g"
   ```
2. Check the file is well-formed before it touches the phone:
   ```bash
   python3 dev/generics/generics.py validate dev/generics/seed-generics.csv
   ```
3. Push it to the phone's Downloads folder:
   ```bash
   python3 dev/generics/generics.py push dev/generics/seed-generics.csv
   # optional: --serial <device-serial>, or set $ANDROID_SERIAL / $ADB
   ```
4. On the phone: **Settings -> Database -> Import CSV food products** -> pick the file from
   Downloads -> import. Column mapping auto-matches because the header names match the
   importer's expected headers exactly.

Re-running `push` + import with the same file is safe: the importer de-dupes by
name+brand+barcode+source, so already-imported rows are skipped.

## CSV format

```
Name,Energy (kcal),Proteins (g),Carbohydrates (g),Fats (g),Note
```

All nutrient values are **per 100 g / kcal per 100 g** (the app's convention). `Name` and
the 4 nutrient columns are required and must be non-blank numbers; `Note` is optional.
This is a deliberately small subset of the ~49 columns the importer supports (brand,
micronutrients, etc.) — add columns later if needed, matching the header strings in
`app/src/commonMain/kotlin/com/maksimowiczm/foodyou/importexport/domain/entity/CsvHeaders.kt`.

`seed-generics.csv` ships a starter list of common everyday foods. **Nutriment values are
approximate** (rounded reference figures) — check/adjust before relying on them for anything
precise.

## `generics.py` commands

- `add` — append one row from CLI flags.
- `validate FILE` — checks the header and every row against the same rules the in-app
  importer enforces, so mistakes are caught before you touch the phone.
- `push FILE` — validates, then `adb push`es the CSV to `/sdcard/Download/`.
- `selftest` — runs the script's own checks (`python3 dev/generics/generics.py selftest`).
