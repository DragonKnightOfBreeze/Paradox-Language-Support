# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

# code_stats.py — Code statistics audit script for Paradox Language Support.
#
# Walks the repository source tree and collects per-language line metrics:
#   - Total / min / max / average lines per file
#   - Blank lines, comment lines, and code lines (non-blank, non-comment)
#
# Supported languages: Kotlin (.kt), Java (.java).
# Comment detection covers single-line (//) and block (/* ... */) comments.
#
# Usage:
#   python scripts/code_stats.py

from __future__ import annotations

import os
from dataclasses import dataclass, field
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
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    targets = ["src", os.path.join("src", "main"), os.path.join("src", "test")]
    languages = [("Kotlin", ".kt"), ("Java", ".java")]

    print("=== Code Statistics Report ===")
    for target in targets:
        target_path = os.path.join(repo_root, target)
        print(f"\nDirectory: {target}")
        if not os.path.isdir(target_path):
            print("  (directory not found, skipped)")
            continue
        for language, extension in languages:
            stats = collect_stats(target_path, extension)
            print_stats(f"{language} ({extension})", stats)


if __name__ == "__main__":
    main()
