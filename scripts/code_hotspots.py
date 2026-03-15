# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

# code_hotspots.py — Code distribution & hotspot audit for Paradox Language Support.
#
# Two complementary views of the same source tree:
#
#   1. Per-package distribution
#      Groups files by their Java/Kotlin package (derived from directory path
#      relative to the source root), then reports file count and line totals
#      for every package, sorted by total lines descending.
#
#   2. Large-file hotspots
#      Lists individual files whose total line count exceeds a configurable
#      threshold (default 500), sorted by line count descending.
#
# Both views cover Kotlin (.kt) and Java (.java) under src/main and src/test.
#
# Output modes:
#   default   : per-package distribution + large-file hotspots
#   --summary : top-N hotspot list + overall stats only
#   --markdown: full markdown document (saved to file)
#
# Output defaults to stdout; use --output FILE to write to a file.
# For --markdown, output defaults to a timestamped file under tmp/reports/.
#
# Usage:
#   python scripts/code_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]

from __future__ import annotations

import argparse
import contextlib
import os
import sys
from collections import defaultdict
from dataclasses import dataclass, field
from datetime import datetime
from typing import Iterable

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

DEFAULT_THRESHOLD = 500

# Source roots relative to repo root — used to derive package names.
# Each entry is (label, relative path to source root).
SOURCE_ROOTS: list[tuple[str, str]] = [
    ("main/kotlin", os.path.join("src", "main", "kotlin")),
    ("main/java",   os.path.join("src", "main", "java")),
    ("test/kotlin", os.path.join("src", "test", "kotlin")),
    ("test/java",   os.path.join("src", "test", "java")),
]

EXTENSIONS = (".kt", ".java")

# ---------------------------------------------------------------------------
# Data model
# ---------------------------------------------------------------------------

@dataclass
class FileRecord:
    """Line metrics for a single source file."""
    path: str          # absolute path
    rel_path: str      # path relative to repo root
    package: str       # dot-separated package name
    source_root: str   # which source root this belongs to
    total: int = 0
    blank: int = 0
    comment: int = 0

    @property
    def code(self) -> int:
        return self.total - self.blank - self.comment


@dataclass
class PackageStats:
    """Aggregated stats for one package across all its files."""
    file_count: int = 0
    total_lines: int = 0
    blank_lines: int = 0
    comment_lines: int = 0

    @property
    def code_lines(self) -> int:
        return self.total_lines - self.blank_lines - self.comment_lines

# ---------------------------------------------------------------------------
# Line counting (shared logic with code_stats.py)
# ---------------------------------------------------------------------------

def count_lines(file_path: str) -> tuple[int, int, int]:
    """Return (total, blank, comment) line counts for a source file.

    Handles // line comments and /* ... */ block comments for Kotlin/Java.
    Lines mixing code and trailing comments are counted as code.
    """
    total = 0
    blank = 0
    comment = 0
    in_block = False

    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            total += 1
            stripped = line.strip()

            if not stripped:
                if in_block:
                    comment += 1
                else:
                    blank += 1
                continue

            if in_block:
                comment += 1
                if "*/" in stripped:
                    in_block = False
                continue

            if stripped.startswith("/*"):
                comment += 1
                if "*/" not in stripped[2:]:
                    in_block = True
                continue

            if stripped.startswith("//"):
                comment += 1
                continue

    return total, blank, comment

# ---------------------------------------------------------------------------
# File discovery & package derivation
# ---------------------------------------------------------------------------

def derive_package(file_path: str, source_root: str) -> str:
    """Derive a dot-separated package name from the file's directory path."""
    rel = os.path.relpath(os.path.dirname(file_path), source_root)
    if rel == ".":
        return "(root)"
    return rel.replace(os.sep, ".")


def collect_records(repo_root: str) -> list[FileRecord]:
    """Walk all source roots and build a FileRecord for every source file."""
    records: list[FileRecord] = []
    for label, rel_root in SOURCE_ROOTS:
        abs_root = os.path.join(repo_root, rel_root)
        if not os.path.isdir(abs_root):
            continue
        for dirpath, _, filenames in os.walk(abs_root):
            for fn in filenames:
                if not any(fn.endswith(ext) for ext in EXTENSIONS):
                    continue
                abs_path = os.path.join(dirpath, fn)
                total, blank, comment = count_lines(abs_path)
                rec = FileRecord(
                    path=abs_path,
                    rel_path=os.path.relpath(abs_path, repo_root),
                    package=derive_package(abs_path, abs_root),
                    source_root=label,
                    total=total,
                    blank=blank,
                    comment=comment,
                )
                records.append(rec)
    return records

# ---------------------------------------------------------------------------
# Report: per-package distribution
# ---------------------------------------------------------------------------

def report_package_distribution(records: list[FileRecord]) -> None:
    """Print per-package file count and line totals, sorted by total lines."""
    # Aggregate by (source_root, package)
    pkg_map: dict[tuple[str, str], PackageStats] = defaultdict(PackageStats)
    for r in records:
        key = (r.source_root, r.package)
        ps = pkg_map[key]
        ps.file_count += 1
        ps.total_lines += r.total
        ps.blank_lines += r.blank
        ps.comment_lines += r.comment

    # Sort by total lines descending
    sorted_pkgs = sorted(pkg_map.items(), key=lambda kv: kv[1].total_lines, reverse=True)

    print("=== Per-Package Code Distribution ===")
    print()
    # Header
    print(f"  {'Source Root':<14} {'Package':<58} {'Files':>5} {'Total':>7} {'Code':>7} {'Comment':>7} {'Blank':>7}")
    print(f"  {'-'*14} {'-'*58} {'-'*5} {'-'*7} {'-'*7} {'-'*7} {'-'*7}")
    for (root, pkg), ps in sorted_pkgs:
        # Truncate long package names for display
        display_pkg = pkg if len(pkg) <= 58 else "..." + pkg[-(58-3):]
        print(f"  {root:<14} {display_pkg:<58} {ps.file_count:>5} {ps.total_lines:>7} {ps.code_lines:>7} {ps.comment_lines:>7} {ps.blank_lines:>7}")

    # Summary
    total_files = sum(ps.file_count for ps in pkg_map.values())
    total_lines = sum(ps.total_lines for ps in pkg_map.values())
    total_code  = sum(ps.code_lines for ps in pkg_map.values())
    total_cmt   = sum(ps.comment_lines for ps in pkg_map.values())
    total_blank = sum(ps.blank_lines for ps in pkg_map.values())
    pkg_count   = len(pkg_map)
    print(f"  {'-'*14} {'-'*58} {'-'*5} {'-'*7} {'-'*7} {'-'*7} {'-'*7}")
    print(f"  {'TOTAL':<14} {f'{pkg_count} packages':<58} {total_files:>5} {total_lines:>7} {total_code:>7} {total_cmt:>7} {total_blank:>7}")

# ---------------------------------------------------------------------------
# Report: large-file hotspots
# ---------------------------------------------------------------------------

def report_hotspots(records: list[FileRecord], threshold: int) -> None:
    """Print files exceeding the line threshold, sorted by total lines."""
    hot = [r for r in records if r.total >= threshold]
    hot.sort(key=lambda r: r.total, reverse=True)

    print()
    print(f"=== Large-File Hotspots (>= {threshold} lines) ===")
    print()

    if not hot:
        print(f"  No files found with >= {threshold} lines.")
        return

    print(f"  {'#':>4} {'Lines':>6} {'Code':>6} {'Cmt':>5} {'Blk':>5}  {'File'}")
    print(f"  {'-'*4} {'-'*6} {'-'*6} {'-'*5} {'-'*5}  {'-'*60}")
    for i, r in enumerate(hot, 1):
        print(f"  {i:>4} {r.total:>6} {r.code:>6} {r.comment:>5} {r.blank:>5}  {r.rel_path}")

    print()
    print(f"  Total hotspot files: {len(hot)} / {len(records)}  ({len(hot)/len(records)*100:.1f}%)")
    hotspot_lines = sum(r.total for r in hot)
    all_lines = sum(r.total for r in records)
    print(f"  Hotspot total lines: {hotspot_lines} / {all_lines}  ({hotspot_lines/all_lines*100:.1f}%)")

# ---------------------------------------------------------------------------
# Report: summary
# ---------------------------------------------------------------------------

_SUMMARY_TOP_N = 10

def report_summary(records: list[FileRecord], threshold: int) -> None:
    """Condensed summary: top-N hotspot list and overall stats."""
    hot = sorted(records, key=lambda r: r.total, reverse=True)[:_SUMMARY_TOP_N]
    all_lines = sum(r.total for r in records)

    print(f"=== Code Hotspot Summary (Top {_SUMMARY_TOP_N}) ===")
    print()
    print(f"  {'#':>4} {'Lines':>6} {'Code':>6} {'Cmt':>5} {'Blk':>5}  {'File'}")
    print(f"  {'-'*4} {'-'*6} {'-'*6} {'-'*5} {'-'*5}  {'-'*60}")
    for i, r in enumerate(hot, 1):
        print(f"  {i:>4} {r.total:>6} {r.code:>6} {r.comment:>5} {r.blank:>5}  {r.rel_path}")
    print()
    hot_all = [r for r in records if r.total >= threshold]
    print(f"  Total files  : {len(records):,}")
    print(f"  Total lines  : {all_lines:,}")
    print(f"  Hotspots (>= {threshold}): {len(hot_all)} files")

# ---------------------------------------------------------------------------
# Report: markdown
# ---------------------------------------------------------------------------

def _md_table(headers: list, rows: list, col_aligns: list[str] | None = None) -> str:
    """Render a GFM markdown table."""
    aligns = col_aligns or ["---"] * len(headers)
    lines = [
        "| " + " | ".join(str(h) for h in headers) + " |",
        "| " + " | ".join(aligns) + " |",
    ]
    for row in rows:
        lines.append("| " + " | ".join(str(c) for c in row) + " |")
    return "\n".join(lines)


def report_markdown(records: list[FileRecord], threshold: int, out) -> None:
    """Write a full markdown report document."""
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    out.write("# Code Distribution & Hotspot Report\n\n")
    out.write(f"> Generated: {ts}\n\n")

    # Per-package distribution
    pkg_map: dict[tuple[str, str], PackageStats] = defaultdict(PackageStats)
    for r in records:
        key = (r.source_root, r.package)
        ps = pkg_map[key]
        ps.file_count += 1
        ps.total_lines += r.total
        ps.blank_lines += r.blank
        ps.comment_lines += r.comment
    sorted_pkgs = sorted(pkg_map.items(), key=lambda kv: kv[1].total_lines, reverse=True)

    out.write("## Per-Package Distribution\n\n")
    headers = ["Source Root", "Package", "Files", "Total", "Code", "Comment", "Blank"]
    aligns = [":---", ":---", "---:", "---:", "---:", "---:", "---:"]
    rows = []
    for (root, pkg), ps in sorted_pkgs:
        rows.append([root, pkg, f"{ps.file_count:,}", f"{ps.total_lines:,}",
                     f"{ps.code_lines:,}", f"{ps.comment_lines:,}", f"{ps.blank_lines:,}"])
    total_files = sum(ps.file_count for ps in pkg_map.values())
    total_lines = sum(ps.total_lines for ps in pkg_map.values())
    total_code  = sum(ps.code_lines  for ps in pkg_map.values())
    total_cmt   = sum(ps.comment_lines for ps in pkg_map.values())
    total_blank = sum(ps.blank_lines for ps in pkg_map.values())
    rows.append(["**TOTAL**", f"**{len(pkg_map)} packages**",
                 f"**{total_files:,}**", f"**{total_lines:,}**",
                 f"**{total_code:,}**", f"**{total_cmt:,}**", f"**{total_blank:,}**"])
    out.write(_md_table(headers, rows, aligns) + "\n\n")

    # Large-file hotspots
    hot = [r for r in records if r.total >= threshold]
    hot.sort(key=lambda r: r.total, reverse=True)
    out.write(f"## Large-File Hotspots (≥ {threshold} lines)\n\n")
    if not hot:
        out.write(f"*No files found with ≥ {threshold} lines.*\n\n")
    else:
        headers = ["#", "Lines", "Code", "Comment", "Blank", "File"]
        aligns  = ["---:", "---:", "---:", "---:", "---:", ":---"]
        rows = []
        for i, r in enumerate(hot, 1):
            rows.append([i, f"{r.total:,}", f"{r.code:,}", f"{r.comment:,}",
                         f"{r.blank:,}", f"`{r.rel_path}`"])
        out.write(_md_table(headers, rows, aligns) + "\n\n")
        hotspot_lines = sum(r.total for r in hot)
        all_lines     = sum(r.total for r in records)
        out.write(f"Hotspot files: **{len(hot):,}** / {len(records):,}  "
                  f"({len(hot)/len(records)*100:.1f}%)  "
                  f"— {hotspot_lines:,} / {all_lines:,} lines "
                  f"({hotspot_lines/all_lines*100:.1f}%)\n\n")

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(description="Code distribution & hotspot audit")
    parser.add_argument("--threshold", type=int, default=DEFAULT_THRESHOLD,
                        help=f"Line threshold for hotspot report (default: {DEFAULT_THRESHOLD})")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--summary", "-s", action="store_true",
                      help="Print condensed summary instead of full report")
    mode.add_argument("--markdown", "--md", dest="markdown", action="store_true",
                      help="Write a full markdown report document")
    parser.add_argument("--output", "-o", metavar="FILE",
                        help="Write output to FILE (for --markdown, defaults to a timestamped file)")
    args = parser.parse_args()

    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    records = collect_records(repo_root)

    if args.markdown:
        if args.output:
            md_path = args.output
        else:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            report_dir = os.path.join(repo_root, "tmp", "reports")
            os.makedirs(report_dir, exist_ok=True)
            md_path = os.path.join(report_dir, f"code_hotspots_{ts}.md")
        with open(md_path, "w", encoding="utf-8") as f:
            report_markdown(records, args.threshold, f)
        print(f"Markdown report written to: {md_path}")
    else:
        out = open(args.output, "w", encoding="utf-8") if args.output else sys.stdout
        with contextlib.redirect_stdout(out):
            if args.summary:
                report_summary(records, args.threshold)
            else:
                report_package_distribution(records)
                report_hotspots(records, args.threshold)
        if args.output:
            out.close()
            print(f"Report written to: {args.output}")


if __name__ == "__main__":
    main()
