# Copyright (c) 2026 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

"""validate_markdown_links.py — Check internal Markdown links for broken targets.

This script scans Markdown files under the configured directories (``documents``
and ``docs`` by default) plus a fixed set of top-level Markdown files (``AGENTS.md``,
``CONTRIBUTING.md``, ``README.md``, ``README_zh.md``), and verifies that every
internal link target resolves to an existing path.

External links (``http://``, ``https://``, ``mailto:``), in-page anchors (``#...``),
and links containing a ``$`` placeholder are ignored. Fenced code blocks and inline
code spans are stripped before scanning.

Usage:
  python scripts/util/validate_markdown_links.py
  python scripts/util/validate_markdown_links.py --dir documents
  python scripts/util/validate_markdown_links.py --file README.md --file AGENTS.md
"""

from __future__ import annotations

import argparse
import os
import re
from pathlib import Path
from urllib.parse import unquote

DEFAULT_SCAN_DIRS: list[str] = ["documents", "docs"]
DEFAULT_SCAN_FILES: list[str] = ["AGENTS.md", "CONTRIBUTING.md", "README.md", "README_zh.md"]

# Match Markdown inline links of the form `[text](target)`.
MD_LINK_PATTERN = re.compile(r"\[([^]]+)]\(([^)]+)\)")
# Strip fenced code blocks and inline code spans so links inside them are ignored.
FENCED_CODE_PATTERN = re.compile(r"```.*?```", re.DOTALL)
INLINE_CODE_PATTERN = re.compile(r"`.*?`")

# Link prefixes that point outside the repository (or within the current page).
EXTERNAL_PREFIXES = ("http://", "https://", "#", "mailto:")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Check internal Markdown links for broken targets.")
    parser.add_argument(
        "--dir",
        dest="scan_dirs",
        action="append",
        metavar="DIR",
        help="Directory to scan recursively (repeatable). Defaults to: " + ", ".join(DEFAULT_SCAN_DIRS),
    )
    parser.add_argument(
        "--file",
        dest="scan_files",
        action="append",
        metavar="FILE",
        help="Top-level Markdown file to check (repeatable). Defaults to the repository root files.",
    )
    return parser.parse_args()


def collect_markdown_files(repo_root: Path, scan_dirs: list[str], scan_files: list[str]) -> list[Path]:
    """Collect every Markdown file to check, scanning files first and then directories."""
    files: list[Path] = []
    for name in scan_files:
        path = repo_root / name
        if path.is_file():
            files.append(path)
    for dir_name in scan_dirs:
        directory = repo_root / dir_name
        if not directory.is_dir():
            continue
        for path in sorted(directory.rglob("*.md")):
            # Skip hidden/dot directories and node_modules.
            parents = path.relative_to(directory).parts[:-1]
            if any(part.startswith(".") or part == "node_modules" for part in parents):
                continue
            files.append(path)
    return files


def resolve_target(source: Path, repo_root: Path, target: str) -> Path | None:
    """Resolve a link *target* to an absolute path, or ``None`` when it has no path part."""
    target_path = target.split("#")[0]
    if not target_path:
        return None
    target_path = unquote(target_path)
    if target_path.startswith("/"):
        resolved = repo_root / target_path.lstrip("/")
    else:
        resolved = source.parent / target_path
    return Path(os.path.normpath(resolved))


def find_broken_links(repo_root: Path, files: list[Path]) -> tuple[int, list[tuple[Path, str, Path]]]:
    """Return the total number of internal links and the list of broken ones."""
    broken: list[tuple[Path, str, Path]] = []
    total = 0
    for source in files:
        text = source.read_text(encoding="utf-8")
        text = FENCED_CODE_PATTERN.sub("", text)
        text = INLINE_CODE_PATTERN.sub("", text)

        for match in MD_LINK_PATTERN.finditer(text):
            target = match.group(2).strip()
            if target.startswith(EXTERNAL_PREFIXES) or "$" in target:
                continue
            total += 1

            resolved = resolve_target(source, repo_root, target)
            if resolved is None:
                continue
            if not resolved.exists():
                broken.append((source, target, resolved))
    return total, broken


def display_path(path: Path, repo_root: Path) -> str:
    """Render *path* relative to *repo_root* when possible, otherwise as-is."""
    if path.is_relative_to(repo_root):
        return str(path.relative_to(repo_root))
    return str(path)


def main() -> int:
    args = parse_args()
    repo_root = Path(__file__).resolve().parents[2]
    scan_dirs = args.scan_dirs or DEFAULT_SCAN_DIRS
    scan_files = args.scan_files or DEFAULT_SCAN_FILES

    files = collect_markdown_files(repo_root, scan_dirs, scan_files)
    total, broken = find_broken_links(repo_root, files)

    print(f"Scanned {total} internal Markdown links across {len(files)} files.")
    if not broken:
        print("All internal Markdown links are valid! 0 broken links found.")
        return 0

    print(f"Found {len(broken)} broken links:")
    for source, target, resolved in broken:
        source_text = display_path(source, repo_root)
        resolved_text = display_path(resolved, repo_root)
        print(f"  In {source_text}: target '{target}' -> '{resolved_text}' NOT FOUND")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
