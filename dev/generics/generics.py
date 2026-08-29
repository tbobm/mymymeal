#!/usr/bin/env python3
"""Bootstrap "Generics" (user-created foods) from the laptop.

Builds a CSV that matches the app's CSV product importer
(Settings -> Database -> Import CSV food products), which hardcodes
source=User for every imported row. Values are per 100 g / kcal per 100 g,
matching the app's assumption.

Header/required-field contract mirrored from:
  app/src/commonMain/.../importexport/domain/entity/CsvHeaders.kt
  app/src/androidMain/.../database/importcsvproducts/ImportCsvProductsViewModel.kt (requiredKeys)
  app/src/commonMain/.../importexport/domain/usecase/ImportCsvProductUseCase.kt (String.toDouble())
"""

import argparse
import csv
import os
import shlex
import subprocess
import sys
import tempfile

HEADER = ["Name", "Energy (kcal)", "Proteins (g)", "Carbohydrates (g)", "Fats (g)", "Note"]
REQUIRED_NUMERIC = HEADER[1:5]  # Energy, Proteins, Carbohydrates, Fats


def _parse_like_app(raw):
    """Mirror ImportCsvProductUseCase.kt's String.toDouble(): strip '<='/'<',
    treat '-'/'null'/blank as absent, else float. Returns (value_or_None, ok)."""
    s = raw.strip().replace("<=", "").replace("<", "").lower()
    if s in ("", "-", "null"):
        return None, True
    try:
        return float(s), True
    except ValueError:
        return None, False


def validate(path):
    errors = []
    with open(path, newline="", encoding="utf-8") as f:
        rows = list(csv.reader(f))

    if not rows:
        return ["file is empty"]

    header = rows[0]
    if header != HEADER:
        errors.append(f"header mismatch:\n  expected {HEADER}\n  got      {header}")
        return errors  # no point checking rows against the wrong header

    for i, row in enumerate(rows[1:], start=2):
        if len(row) != len(header):
            errors.append(f"line {i}: expected {len(header)} columns, got {len(row)}")
            continue
        cells = dict(zip(header, row))
        if not cells["Name"].strip():
            errors.append(f"line {i}: Name is required")
        for col in REQUIRED_NUMERIC:
            value, ok = _parse_like_app(cells[col])
            if not ok:
                errors.append(f"line {i}: {col!r}={cells[col]!r} is not numeric")
            elif value is None:
                errors.append(f"line {i}: {col!r} is required (blank/-/null not allowed)")

    return errors


def cmd_validate(args):
    errors = validate(args.file)
    if errors:
        print(f"{args.file}: {len(errors)} problem(s):", file=sys.stderr)
        for e in errors:
            print(f"  - {e}", file=sys.stderr)
        return 1
    with open(args.file, newline="", encoding="utf-8") as f:
        n = sum(1 for _ in f) - 1
    print(f"{args.file}: OK ({n} generic food(s))")
    return 0


def cmd_add(args):
    is_new = not os.path.exists(args.file) or os.path.getsize(args.file) == 0
    with open(args.file, "a", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        if is_new:
            writer.writerow(HEADER)
        writer.writerow(
            [args.name, args.kcal, args.protein, args.carbs, args.fat, args.note or ""]
        )
    errors = validate(args.file)
    if errors:
        print(f"appended row is invalid, see {args.file}:", file=sys.stderr)
        for e in errors:
            print(f"  - {e}", file=sys.stderr)
        return 1
    print(f"added {args.name!r} to {args.file}")
    return 0


def _adb_push_cmd(adb_env, serial, src, dest):
    """Build the argv for `adb push`. $ADB may be a multi-word command
    (e.g. "rtk proxy /path/to/adb"), so split it rather than treat it as one token."""
    cmd = shlex.split(adb_env)
    if serial:
        cmd += ["-s", serial]
    cmd += ["push", src, dest]
    return cmd


def cmd_push(args):
    if validate(args.file):
        print("fix validation errors before pushing (run: validate)", file=sys.stderr)
        return 1
    dest = "/sdcard/Download/" + os.path.basename(args.file)
    cmd = _adb_push_cmd(
        os.environ.get("ADB", "adb"),
        args.serial or os.environ.get("ANDROID_SERIAL"),
        args.file,
        dest,
    )
    result = subprocess.run(cmd)
    if result.returncode != 0:
        return result.returncode
    print(f"pushed to {dest}")
    print("Now on the phone: Settings -> Database -> Import CSV food products -> pick the file.")
    return 0


def cmd_selftest(_args):
    with tempfile.TemporaryDirectory() as tmp:
        path = os.path.join(tmp, "t.csv")

        class NS:
            pass

        a = NS()
        a.file, a.name, a.kcal, a.protein, a.carbs, a.fat, a.note = (
            path,
            "Test food",
            "100",
            "1",
            "2",
            "3",
            "",
        )
        assert cmd_add(a) == 0, "add should succeed"
        assert validate(path) == [], "freshly added row should validate clean"

        with open(path, "a", newline="", encoding="utf-8") as f:
            csv.writer(f).writerow(["No numbers", "abc", "1", "2", "3", ""])
        errs = validate(path)
        assert any("not numeric" in e for e in errs), f"expected a not-numeric error, got {errs}"

        with open(path, "a", newline="", encoding="utf-8") as f:
            csv.writer(f).writerow(["", "10", "1", "2", "3", ""])
        errs = validate(path)
        assert any("Name is required" in e for e in errs), f"expected a missing-name error, got {errs}"

        bad_header_path = os.path.join(tmp, "bad_header.csv")
        with open(bad_header_path, "w", newline="", encoding="utf-8") as f:
            csv.writer(f).writerow(["Name", "Calories"])
        errs = validate(bad_header_path)
        assert any("header mismatch" in e for e in errs), f"expected header mismatch, got {errs}"

    # $ADB can be a multi-word command; must not be passed to subprocess as one token.
    cmd = _adb_push_cmd("rtk proxy /path/to/adb", "SERIAL123", "local.csv", "/sdcard/Download/local.csv")
    assert cmd == [
        "rtk",
        "proxy",
        "/path/to/adb",
        "-s",
        "SERIAL123",
        "push",
        "local.csv",
        "/sdcard/Download/local.csv",
    ], cmd
    assert _adb_push_cmd("adb", None, "a.csv", "/sdcard/Download/a.csv") == [
        "adb",
        "push",
        "a.csv",
        "/sdcard/Download/a.csv",
    ]

    print("selftest OK")
    return 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)

    p_validate = sub.add_parser("validate", help="check a CSV against the importer's rules")
    p_validate.add_argument("file")
    p_validate.set_defaults(func=cmd_validate)

    p_add = sub.add_parser("add", help="append one generic food to a CSV")
    p_add.add_argument("--file", default="dev/generics/seed-generics.csv")
    p_add.add_argument("--name", required=True)
    p_add.add_argument("--kcal", required=True, type=float)
    p_add.add_argument("--protein", required=True, type=float)
    p_add.add_argument("--carbs", required=True, type=float)
    p_add.add_argument("--fat", required=True, type=float)
    p_add.add_argument("--note", default="")
    p_add.set_defaults(func=cmd_add)

    p_push = sub.add_parser("push", help="validate then adb push a CSV to the phone's Downloads")
    p_push.add_argument("file")
    p_push.add_argument("--serial", help="adb device serial (default: $ANDROID_SERIAL)")
    p_push.set_defaults(func=cmd_push)

    p_selftest = sub.add_parser("selftest", help="run this script's self-checks")
    p_selftest.set_defaults(func=cmd_selftest)

    args = parser.parse_args()
    sys.exit(args.func(args))


if __name__ == "__main__":
    main()
