# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

# config_stats.py — CWT config file statistics audit for Paradox Language Support.
#
# Walks the cwt/ directory tree and collects per-repository line metrics
# for CWT config files (.cwt):
#   - Total / min / max / average lines per file
#   - Blank lines, comment lines, and code lines
#
# CWT has three kinds of comment lines:
#   - Line comment:     starts with a single '#' (not '##')
#   - Option comment:   starts with '##' (not '###') — carries metadata,
#                        counted as CODE (not comment)
#   - Doc comment:      starts with '###' or more '#'s — counted as comment
#
# Only line comments and doc comments are tallied as "comment lines".
# Option comments (##) are considered part of the effective config and
# therefore counted as code lines.
#
# Usage:
#   python scripts/config_stats.py

from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Iterable

# ---------------------------------------------------------------------------
# Data model
# ---------------------------------------------------------------------------

@dataclass
class FileLineInfo:
    """Per-file line breakdown returned by count_file_lines()."""
    total: int = 0
    blank: int = 0
    comment: int = 0  # line comments (#) + doc comments (###)

    @property
    def code(self) -> int:
        """Lines that are neither blank nor comment (includes option comments)."""
        return self.total - self.blank - self.comment


@dataclass
class Stats:
    """Aggregated statistics for a set of CWT config files."""
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
# Line counting (CWT comment-aware)
# ---------------------------------------------------------------------------

def count_file_lines(file_path: str) -> FileLineInfo:
    """Count total, blank, and comment lines in a CWT config file.

    Comment classification for CWT:
      - '###' (or more '#'s): doc comment  -> counted as comment
      - '##' (exactly two):   option comment -> counted as code (metadata)
      - '#' (single):         line comment  -> counted as comment
    """
    info = FileLineInfo()

    with open(file_path, "r", encoding="utf-8", errors="ignore") as handle:
        for line in handle:
            info.total += 1
            stripped = line.strip()

            # Blank line
            if not stripped:
                info.blank += 1
                continue

            # Only classify lines whose first non-whitespace char is '#'
            if stripped.startswith("#"):
                # Count leading '#' characters
                hashes = 0
                for ch in stripped:
                    if ch == "#":
                        hashes += 1
                    else:
                        break

                if hashes >= 3:
                    # Doc comment (### or more) -> comment
                    info.comment += 1
                elif hashes == 2:
                    # Option comment (##) -> code (metadata), do not count as comment
                    pass
                else:
                    # Line comment (single #) -> comment
                    info.comment += 1
                continue

            # Non-comment, non-blank -> code line (counted implicitly)

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
    """Print a formatted summary block for one directory."""
    print(f"  [{label}]")
    print(f"    Files        : {stats.file_count}")
    if stats.file_count == 0:
        return
    print(f"    Total lines  : {stats.total_lines}")
    print(f"    Min lines    : {stats.min_lines}")
    print(f"    Max lines    : {stats.max_lines}")
    print(f"    Avg lines    : {stats.avg_lines:.1f}")
    print(f"    Blank lines  : {stats.blank_lines}")
    print(f"    Comment lines: {stats.comment_lines}")
    print(f"    Code lines   : {stats.code_lines}")

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    cwt_root = os.path.join(repo_root, "cwt")
    if not os.path.isdir(cwt_root):
        print("Error: cwt/ directory not found, cannot collect statistics.")
        return

    print("=== CWT Config Statistics Report ===")
    for entry in sorted(os.scandir(cwt_root), key=lambda item: item.name):
        if not entry.is_dir():
            continue
        relative_path = os.path.join("cwt", entry.name)
        print(f"\nDirectory: {relative_path}")
        stats = collect_stats(entry.path, ".cwt")
        print_stats("CWT (.cwt)", stats)


if __name__ == "__main__":
    main()
