# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

"""cleanup_changelog.py — Normalize CHANGELOG issue links and released-version dates.

This script updates CHANGELOG.md in two ways:
1. Replace standalone issue IDs in list items, such as ` #123`, with Markdown links.
2. Append release dates to released version headings, such as `## 2.0.0 - 2026-01-01`.

Date source:
- Prefer the date of the corresponding git tag in the local repository.
- The tag name is resolved as `v<version>` first, then `<version>`.

Usage:
  python scripts/util/cleanup_changelog.py
  python scripts/util/cleanup_changelog.py --check
  python scripts/util/cleanup_changelog.py --file CHANGELOG.md
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path

REPO_URL = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support"
ISSUE_URL_TEMPLATE = REPO_URL + "/issues/{issue_id}"
# Match only standalone issue references with a leading whitespace and a trailing whitespace or end-of-line.
ISSUE_ID_PATTERN = re.compile(r"(?<=\s)#(\d+)(?=\s|$)")
# Match second-level headings that start with a plain version, optionally followed by an existing date.
VERSION_HEADING_PATTERN = re.compile(r"^(##\s+)(\d+(?:\.\d+)+(?:-[A-Za-z0-9.-]+)?)(?:\s+-\s+(\d{4}-\d{2}-\d{2}))?\s*$")
DATE_PATTERN = re.compile(r"\d{4}-\d{2}-\d{2}")
TAG_DATE_CACHE: dict[str, str | None] = {}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Normalize CHANGELOG issue links and release dates.")
    parser.add_argument(
        "--file",
        type=Path,
        default=Path("CHANGELOG.md"),
        help="Path to the changelog file relative to the repository root.",
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Check whether the file is already normalized without writing changes.",
    )
    return parser.parse_args()


def run_git(repo_root: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", *args],
        cwd=repo_root,
        check=False,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    if result.returncode != 0:
        return ""
    return result.stdout.strip()


def get_tag_date(repo_root: Path, version: str) -> str | None:
    if version in TAG_DATE_CACHE:
        return TAG_DATE_CACHE[version]

    # Prefer the GitHub-style tag name with the `v` prefix, then fall back to the raw version.
    for tag_name in (f"v{version}", version):
        date_text = run_git(repo_root, "log", "-1", "--format=%as", tag_name)
        if DATE_PATTERN.fullmatch(date_text):
            TAG_DATE_CACHE[version] = date_text
            return date_text

    heading_marker = f"## {version}"
    history_date = run_git(repo_root, "log", "--reverse", "--format=%as", "-S", heading_marker, "--", "CHANGELOG.md")
    if history_date:
        first_line = history_date.splitlines()[0].strip()
        if DATE_PATTERN.fullmatch(first_line):
            TAG_DATE_CACHE[version] = first_line
            return first_line

    TAG_DATE_CACHE[version] = None
    return None


def normalize_issue_links(line: str) -> str:
    def replacer(match: re.Match[str]) -> str:
        issue_id = match.group(1)
        return f"[#{issue_id}]({ISSUE_URL_TEMPLATE.format(issue_id=issue_id)})"

    return ISSUE_ID_PATTERN.sub(replacer, line)


def normalize_version_heading(line: str, repo_root: Path) -> str:
    match = VERSION_HEADING_PATTERN.match(line)
    if not match:
        return line

    prefix, version, _existing_date = match.groups()
    # Keep Unreleased and development headings unchanged.
    if version == "Unreleased" or version.endswith("-dev"):
        return line

    tag_date = get_tag_date(repo_root, version)
    if tag_date is None:
        return line
    return f"{prefix}{version} - {tag_date}"


def normalize_changelog(text: str, repo_root: Path) -> str:
    normalized_lines: list[str] = []
    for line in text.splitlines():
        normalized_line = normalize_issue_links(line)
        normalized_line = normalize_version_heading(normalized_line, repo_root)
        normalized_lines.append(normalized_line)

    trailing_newline = "\n" if text.endswith("\n") else ""
    return "\n".join(normalized_lines) + trailing_newline


def main() -> int:
    args = parse_args()
    repo_root = Path(__file__).resolve().parents[2]
    changelog_file = (repo_root / args.file).resolve()

    if not changelog_file.is_file():
        print(f"ERROR: changelog file not found: {changelog_file}", file=sys.stderr)
        return 1

    original_text = changelog_file.read_text(encoding="utf-8")
    normalized_text = normalize_changelog(original_text, repo_root)

    if args.check:
        if normalized_text != original_text:
            print(f"CHANGELOG requires normalization: {changelog_file}")
            return 1
        print(f"CHANGELOG is already normalized: {changelog_file}")
        return 0

    if normalized_text == original_text:
        print(f"No changes needed: {changelog_file}")
        return 0

    changelog_file.write_text(normalized_text, encoding="utf-8", newline="\n")
    print(f"Updated: {changelog_file}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
