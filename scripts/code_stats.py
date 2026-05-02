# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

"""code_stats.py — Code statistics audit script for the plugin.

Walks the repository source tree and collects per-language line metrics:
  - Total / min / max / average lines per file
  - Blank lines, comment lines, and code lines (non-blank, non-comment)

Supported languages: Kotlin (.kt), Java (.java).
Comment detection covers single-line (//) and block (/* ... */) comments.

Output modes:
  default   : per-directory detailed stats block
  --summary : condensed table with one row per directory
  --markdown: full markdown document (saved to file)

Output defaults to stdout; use --output FILE to write to a file.
For --markdown, output defaults to a timestamped file under tmp/reports/.

Usage:
  python scripts/code_stats.py [--summary] [--markdown] [--output FILE]
"""

from __future__ import annotations

import argparse
import contextlib
import os
import sys
from dataclasses import dataclass
from datetime import datetime
from typing import Iterable


# ---------------------------------------------------------------------------
# Data model
# ---------------------------------------------------------------------------

@dataclass
class FileLineInfo:
    """Per-file line breakdown returned by count_file_lines()."""
    total: int = 0
    blank: int = 0
    comment: int = 0

    @property
    def code(self) -> int:
        """Lines that are neither blank nor comment."""
        return self.total - self.blank - self.comment


@dataclass
class Stats:
    """Aggregated statistics for a set of source files."""
    file_count: int = 0
    total_lines: int = 0
    min_lines: int = 0
    max_lines: int = 0
    blank_lines: int = 0
    comment_lines: int = 0

    @property
    def code_lines(self) -> int:
        return self.total_lines - self.blank_lines - self.comment_lines

    @property
    def avg_lines(self) -> float:
        if self.file_count == 0:
            return 0.0
        return self.total_lines / self.file_count

# ---------------------------------------------------------------------------
# File traversal
# ---------------------------------------------------------------------------

def iter_files(base_dir: str, extensions: Iterable[str]) -> Iterable[str]:
    """Yield absolute paths of files matching any of the given extensions."""
    for root, _, files in os.walk(base_dir):
        for filename in files:
            if any(filename.endswith(ext) for ext in extensions):
                yield os.path.join(root, filename)

# ---------------------------------------------------------------------------
# Line counting (with comment awareness)
# ---------------------------------------------------------------------------

def count_file_lines(file_path: str) -> FileLineInfo:
    """Count total, blank, and comment lines in a single source file.

    Handles single-line comments (//) and block comments (/* ... */).
    A line that is entirely whitespace or entirely a comment is classified
    accordingly; lines mixing code and trailing comments count as code.
    """
    info = FileLineInfo()
    in_block_comment = False

    with open(file_path, "r", encoding="utf-8", errors="ignore") as handle:
        for line in handle:
            info.total += 1
            stripped = line.strip()

            # Blank line (even inside a block comment we still count as comment)
            if not stripped:
                if in_block_comment:
                    info.comment += 1
                else:
                    info.blank += 1
                continue

            if in_block_comment:
                # Still inside a block comment
                info.comment += 1
                if "*/" in stripped:
                    in_block_comment = False
                continue

            # Check for block comment start
            if stripped.startswith("/*"):
                info.comment += 1
                # Single-line block comment: /* ... */
                if "*/" in stripped[2:]:
                    pass  # block opens and closes on same line
                else:
                    in_block_comment = True
                continue

            # Single-line comment
            if stripped.startswith("//"):
                info.comment += 1
                continue

            # Otherwise it is a code line (trailing comments still count as code)

    return info

# ---------------------------------------------------------------------------
# Aggregation
# ---------------------------------------------------------------------------

def collect_stats(base_dir: str, extension: str) -> Stats:
    """Walk base_dir, aggregate line stats for files with the given extension."""
    stats = Stats()
    for file_path in iter_files(base_dir, [extension]):
        info = count_file_lines(file_path)
        stats.file_count += 1
        stats.total_lines += info.total
        stats.blank_lines += info.blank
        stats.comment_lines += info.comment
        # Track per-file min/max based on total lines
        if stats.file_count == 1:
            stats.min_lines = info.total
            stats.max_lines = info.total
        else:
            stats.min_lines = min(stats.min_lines, info.total)
            stats.max_lines = max(stats.max_lines, info.total)
    return stats

# ---------------------------------------------------------------------------
# Reporting
# ---------------------------------------------------------------------------

def print_stats(label: str, stats: Stats) -> None:
    """Print a formatted summary block for one language / directory pair."""
    print(f"  [{label}]")
    print(f"    Files       : {stats.file_count}")
    if stats.file_count == 0:
        return
    print(f"    Total lines : {stats.total_lines}")
    print(f"    Min lines   : {stats.min_lines}")
    print(f"    Max lines   : {stats.max_lines}")
    print(f"    Avg lines   : {stats.avg_lines:.1f}")
    print(f"    Blank lines : {stats.blank_lines}")
    print(f"    Comment lines: {stats.comment_lines}")
    print(f"    Code lines  : {stats.code_lines}")

# ---------------------------------------------------------------------------
# Data collection
# ---------------------------------------------------------------------------

def collect_dir_data(
    repo_root: str,
    targets: list[str],
    languages: list[tuple[str, str]],
) -> list[tuple[str, bool, dict[str, Stats]]]:
    """Collect stats for each (target, language) pair.

    Returns list of (target_rel_path, dir_exists, {language_name: Stats}).
    """
    result = []
    for target in targets:
        target_path = os.path.join(repo_root, target)
        exists = os.path.isdir(target_path)
        lang_stats: dict[str, Stats] = {
            lang: collect_stats(target_path, ext) if exists else Stats()
            for lang, ext in languages
        }
        result.append((target, exists, lang_stats))
    return result

# ---------------------------------------------------------------------------
# Report: detailed
# ---------------------------------------------------------------------------

def report_detailed(
    dir_data: list[tuple[str, bool, dict[str, Stats]]],
    languages: list[tuple[str, str]],
) -> None:
    """Per-directory detailed stats block."""
    print("=== Code Statistics Report ===")
    for target, exists, lang_stats in dir_data:
        print(f"\nDirectory: {target}")
        if not exists:
            print("  (directory not found, skipped)")
            continue
        for language, extension in languages:
            print_stats(f"{language} ({extension})", lang_stats[language])

# ---------------------------------------------------------------------------
# Report: summary
# ---------------------------------------------------------------------------

def report_summary(
    dir_data: list[tuple[str, bool, dict[str, Stats]]],
    languages: list[tuple[str, str]],
) -> None:
    """Condensed table with one row per directory."""
    print("=== Code Statistics Summary ===")
    print()
    w = max((len(t) for t, _, _ in dir_data), default=12)
    w = max(w, 12)
    lang_names = [lang for lang, _ in languages]
    hdr = ([f"{'Directory':<{w}}"] +
           [f"{n+'.Files':>12}" for n in lang_names] +
           [f"{n+'.Lines':>12}" for n in lang_names] +
           [f"{'TotalLines':>12}"])
    sep = ['-' * w] + ['-' * 12] * (len(lang_names) * 2 + 1)
    print("  " + "  ".join(hdr))
    print("  " + "  ".join(sep))
    for target, exists, lang_stats in dir_data:
        if not exists:
            continue
        row = ([f"{target:<{w}}"] +
               [f"{lang_stats[n].file_count:>12,}" for n in lang_names] +
               [f"{lang_stats[n].total_lines:>12,}" for n in lang_names] +
               [f"{sum(lang_stats[n].total_lines for n in lang_names):>12,}"])
        print("  " + "  ".join(row))
    print()
    print("  Note: src includes src/main and src/test; totals overlap.")

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


def report_markdown(
    dir_data: list[tuple[str, bool, dict[str, Stats]]],
    languages: list[tuple[str, str]],
    out,
) -> None:
    """Write a full Markdown report document."""
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    out.write("# Code Statistics Report\n\n")
    out.write(f"> Generated: {ts}\n\n")
    aligns = [":---"] + ["---:"] * 8
    for target, exists, lang_stats in dir_data:
        out.write(f"## {target}\n\n")
        if not exists:
            out.write("*(directory not found, skipped)*\n\n")
            continue
        headers = ["Language", "Files", "Total", "Min", "Max", "Avg", "Code", "Comment", "Blank"]
        rows = []
        for lang, ext in languages:
            s = lang_stats[lang]
            if s.file_count == 0:
                rows.append([f"{lang} (`{ext}`)", 0, 0, "-", "-", "-", 0, 0, 0])
            else:
                rows.append([
                    f"{lang} (`{ext}`)",
                    f"{s.file_count:,}", f"{s.total_lines:,}",
                    f"{s.min_lines:,}", f"{s.max_lines:,}", f"{s.avg_lines:.1f}",
                    f"{s.code_lines:,}", f"{s.comment_lines:,}", f"{s.blank_lines:,}",
                ])
        out.write(_md_table(headers, rows, aligns) + "\n\n")

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(description="Code statistics audit")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--summary", "-s", action="store_true",
                      help="Print condensed summary table")
    mode.add_argument("--markdown", "--md", dest="markdown", action="store_true",
                      help="Write a full Markdown report document")
    parser.add_argument("--output", "-o", metavar="FILE",
                        help="Write output to FILE (for --markdown, defaults to a timestamped file)")
    args = parser.parse_args()

    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    targets = ["src", os.path.join("src", "main"), os.path.join("src", "test")]
    languages = [("Kotlin", ".kt"), ("Java", ".java")]
    dir_data = collect_dir_data(repo_root, targets, languages)

    if args.markdown:
        if args.output:
            md_path = args.output
        else:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            report_dir = os.path.join(repo_root, "tmp", "reports")
            os.makedirs(report_dir, exist_ok=True)
            md_path = os.path.join(report_dir, f"code_stats_{ts}.md")
        with open(md_path, "w", encoding="utf-8") as f:
            report_markdown(dir_data, languages, f)
        print(f"Markdown report written to: {md_path}")
    else:
        out = open(args.output, "w", encoding="utf-8") if args.output else sys.stdout
        with contextlib.redirect_stdout(out):
            if args.summary:
                report_summary(dir_data, languages)
            else:
                report_detailed(dir_data, languages)
        if args.output:
            out.close()
            print(f"Report written to: {args.output}")


if __name__ == "__main__":
    main()
