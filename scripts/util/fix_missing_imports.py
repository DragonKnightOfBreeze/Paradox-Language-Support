# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

"""fix_missing_imports.py — Add missing explicit imports to Kotlin files.

This script scans Kotlin source files under ``src/main/kotlin`` and
``src/test/kotlin``, and for each occurrence of a predefined identifier that is
used but not yet explicitly imported (or covered by a wildcard import), appends
the corresponding import statement after the last existing import line.

The list of fully-qualified names that require explicit imports is maintained
in the ``FQ_NAME_MAP`` dictionary and can be extended as needed.

Usage:
  python scripts/util/fix_missing_imports.py
"""

from __future__ import annotations

import re
from pathlib import Path

# Fully-qualified name -> simple identifier mapping.
# Add entries here when new model classes need explicit imports in other packages.
FQ_NAME_MAP: dict[str, str] = {
    "icu.windea.pls.model.CwtType": "CwtType",
    "icu.windea.pls.model.CwtSeparatorType": "CwtSeparatorType",
    "icu.windea.pls.model.ParadoxGameType": "ParadoxGameType",
    "icu.windea.pls.model.ParadoxDefinitionSource": "ParadoxDefinitionSource",
    "icu.windea.pls.model.ParadoxLocalisationType": "ParadoxLocalisationType",
}

# Directories to scan for Kotlin files, relative to the repository root.
SCAN_DIRS: list[str] = [
    "src/main/kotlin",
    "src/test/kotlin",
]


def _package_of(fq_name: str) -> str:
    """Extract the package portion from a fully-qualified name."""
    return ".".join(fq_name.split(".")[:-1])


def _is_imported(import_stmts: list[str], fq_name: str, pkg: str) -> bool:
    """Check whether *fq_name* is already covered by an existing import.

    Returns ``True`` when the file already contains:

    - an exact import of *fq_name*, or
    - a wildcard import for the package *pkg*.

    Aliased imports (``import ... as X``) count as matching the FQName itself.
    """
    for stmt in import_stmts:
        base = stmt.removeprefix("import ").strip()
        base = base.split(" as ")[0].strip()
        if base == fq_name:
            return True
        if base.endswith(".*") and base[:-2] == pkg:
            return True
    return False


def _file_package(lines: list[str]) -> str | None:
    """Extract the package name from the first ``package ...`` declaration."""
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("package "):
            return stripped.removeprefix("package ").strip()
    return None


def process_file(file_path: Path) -> bool:
    """Scan a single Kotlin file and add any missing import statements.

    Returns ``True`` if the file was modified.
    """
    content = file_path.read_text(encoding="utf-8")
    lines = content.splitlines(keepends=True)

    file_pkg = _file_package(lines)

    import_indices: list[int] = []
    import_stmts: list[str] = []
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith("import "):
            import_indices.append(i)
            import_stmts.append(stripped)

    imports_needed: list[str] = []
    for fq_name, identifier in FQ_NAME_MAP.items():
        pkg = _package_of(fq_name)
        if pkg == file_pkg:
            continue  # same package — no import needed
        if _is_imported(import_stmts, fq_name, pkg):
            continue

        pattern = rf"\b{re.escape(identifier)}\b"
        found = False
        for i, line in enumerate(lines):
            stripped = line.strip()
            if stripped.startswith("import ") and fq_name in stripped:
                continue
            if stripped.startswith("package "):
                continue
            if re.search(pattern, line):
                found = True
                break

        if found:
            imports_needed.append(f"import {fq_name}\n")

    if not imports_needed:
        return False

    if import_indices:
        insert_idx = import_indices[-1] + 1
    else:
        # No existing import statements — place after the package declaration.
        insert_idx = 0
        for i, line in enumerate(lines):
            if line.strip().startswith("package "):
                insert_idx = i + 1
                break
        imports_needed.insert(0, "\n")

    new_lines = lines[:insert_idx] + imports_needed + lines[insert_idx:]
    file_path.write_text("".join(new_lines), encoding="utf-8")
    print(f"  Modified: {file_path}")
    return True


def main() -> int:
    """Entry point."""
    repo_root = Path(__file__).resolve().parents[2]

    for scan_dir in SCAN_DIRS:
        if not (repo_root / scan_dir).exists():
            print(f"ERROR: directory not found: {repo_root / scan_dir}")
            return 1

    total = 0
    modified = 0

    for scan_dir in SCAN_DIRS:
        print(f"\nScanning: {scan_dir}")
        for kt_file in sorted((repo_root / scan_dir).rglob("*.kt")):
            total += 1
            if process_file(kt_file):
                modified += 1

    print(f"\nDone. Scanned {total} files, modified {modified}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
