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
# Auto-generated files are detected and reported separately:
#   - Filename contains '.gen.' (e.g. modifiers.gen.cwt)
#   - Zero comments AND zero blank lines AND total > 500 lines
#
# Output modes:
#   default   : per-repository detailed stats block
#   --summary : condensed table with one row per repository
#   --markdown: full markdown document (saved to file)
#
# Output defaults to stdout; use --output FILE to write to a file.
# For --markdown, output defaults to a timestamped file under tmp/reports/.
#
# Usage:
#   python scripts/config_stats.py [--summary] [--markdown] [--output FILE]

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
# Generated-file detection
# ---------------------------------------------------------------------------

_CWT_GEN_HEURISTIC_THRESHOLD = 500

def is_generated_cwt(filename: str, total: int, blank: int, comment: int) -> bool:
    """Detect likely auto-generated CWT config files.

    Heuristics:
      - Filename contains '.gen.' (e.g. modifiers.gen.cwt)
      - Zero comments AND zero blank lines AND total > threshold
    """
    if ".gen." in filename:
        return True
    if total > _CWT_GEN_HEURISTIC_THRESHOLD and comment == 0 and blank == 0:
        return True
    return False

# ---------------------------------------------------------------------------
# Aggregation
# ---------------------------------------------------------------------------

def _add_to_stats(stats: Stats, info: FileLineInfo) -> None:
    """Add one file's info into an aggregate Stats object."""
    stats.file_count += 1
    stats.total_lines += info.total
    stats.blank_lines += info.blank
    stats.comment_lines += info.comment
    if stats.file_count == 1:
        stats.min_lines = info.total
        stats.max_lines = info.total
    else:
        stats.min_lines = min(stats.min_lines, info.total)
        stats.max_lines = max(stats.max_lines, info.total)


def collect_stats(base_dir: str, extension: str) -> tuple[Stats, Stats]:
    """Walk base_dir, aggregate line stats for files with the given extension.

    Returns (normal_stats, generated_stats).
    """
    normal = Stats()
    generated = Stats()
    for file_path in iter_files(base_dir, [extension]):
        fn = os.path.basename(file_path)
        info = count_file_lines(file_path)
        target = generated if is_generated_cwt(fn, info.total, info.blank, info.comment) else normal
        _add_to_stats(target, info)
    return normal, generated

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

def _merge_stats(a: Stats, b: Stats) -> Stats:
    """Merge two Stats objects into a new combined Stats."""
    merged = Stats()
    merged.file_count = a.file_count + b.file_count
    merged.total_lines = a.total_lines + b.total_lines
    merged.blank_lines = a.blank_lines + b.blank_lines
    merged.comment_lines = a.comment_lines + b.comment_lines
    if a.file_count > 0 and b.file_count > 0:
        merged.min_lines = min(a.min_lines, b.min_lines)
        merged.max_lines = max(a.max_lines, b.max_lines)
    elif a.file_count > 0:
        merged.min_lines = a.min_lines
        merged.max_lines = a.max_lines
    elif b.file_count > 0:
        merged.min_lines = b.min_lines
        merged.max_lines = b.max_lines
    return merged


def report_detailed(dir_data: list) -> None:
    """Per-repository detailed stats block."""
    print("=== CWT Config Statistics Report ===")
    for repo_name, rel_path, combined, generated in dir_data:
        print(f"\nDirectory: {rel_path}")
        print_stats("CWT (.cwt)", combined)

    grand_generated = Stats()
    for _, _, _, generated in dir_data:
        if generated.file_count > 0:
            grand_generated = _merge_stats(grand_generated, generated)
    if grand_generated.file_count > 0:
        print(f"\n=== Generated File Summary ===")
        print_stats("Generated CWT (.cwt)", grand_generated)


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


def report_summary(dir_data: list) -> None:
    """Condensed table with one row per repository."""
    print("=== CWT Config Statistics Summary ===")
    print()
    w = 32
    hdr = f"  {'Repository':<{w}} {'Files':>6}  {'Total':>8}  {'Code':>8}  {'Cmt':>7}  {'Blk':>7}"
    sep = f"  {'-'*w} {'-'*6}  {'-'*8}  {'-'*8}  {'-'*7}  {'-'*7}"
    print(hdr)
    print(sep)

    grand = Stats()
    grand_gen = Stats()
    for repo_name, rel_path, combined, generated in dir_data:
        if combined.file_count == 0:
            continue
        name = repo_name[:w]
        print(f"  {name:<{w}} {combined.file_count:>6,}  {combined.total_lines:>8,}  {combined.code_lines:>8,}  {combined.comment_lines:>7,}  {combined.blank_lines:>7,}")
        grand = _merge_stats(grand, combined)
        if generated.file_count > 0:
            grand_gen = _merge_stats(grand_gen, generated)

    print(sep)
    print(f"  {'TOTAL':<{w}} {grand.file_count:>6,}  {grand.total_lines:>8,}  {grand.code_lines:>8,}  {grand.comment_lines:>7,}  {grand.blank_lines:>7,}")
    if grand_gen.file_count > 0:
        all_lines = grand.total_lines
        gen_pct_f = grand_gen.file_count / grand.file_count * 100 if grand.file_count else 0.0
        gen_pct_l = grand_gen.total_lines / all_lines * 100 if all_lines else 0.0
        print(f"\n  Generated: {grand_gen.file_count} file(s) ({gen_pct_f:.1f}%), {grand_gen.total_lines:,} lines ({gen_pct_l:.1f}%)")


def report_markdown(dir_data: list, out) -> None:
    """Write a full Markdown report document."""
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    out.write("# CWT Config Statistics Report\n\n")
    out.write(f"> Generated: {ts}\n\n")

    headers = ["Repository", "Files", "Total", "Code", "Comment", "Blank"]
    aligns = [":---", "---:", "---:", "---:", "---:", "---:"]

    out.write("## Repository Summary\n\n")
    rows = []
    grand = Stats()
    grand_gen = Stats()
    for repo_name, rel_path, combined, generated in dir_data:
        if combined.file_count == 0:
            continue
        rows.append([
            f"`{repo_name}`",
            f"{combined.file_count:,}", f"{combined.total_lines:,}",
            f"{combined.code_lines:,}", f"{combined.comment_lines:,}", f"{combined.blank_lines:,}",
        ])
        grand = _merge_stats(grand, combined)
        if generated.file_count > 0:
            grand_gen = _merge_stats(grand_gen, generated)
    rows.append([
        "**TOTAL**",
        f"**{grand.file_count:,}**", f"**{grand.total_lines:,}**",
        f"**{grand.code_lines:,}**", f"**{grand.comment_lines:,}**", f"**{grand.blank_lines:,}**",
    ])
    out.write(_md_table(headers, rows, aligns) + "\n\n")

    if grand_gen.file_count > 0:
        all_lines = grand.total_lines
        gen_pct_f = grand_gen.file_count / grand.file_count * 100 if grand.file_count else 0.0
        gen_pct_l = grand_gen.total_lines / all_lines * 100 if all_lines else 0.0
        out.write("## Generated Files\n\n")
        out.write(f"Auto-detected: **{grand_gen.file_count}** / {grand.file_count:,} files "
                  f"({gen_pct_f:.1f}%), "
                  f"**{grand_gen.total_lines:,}** / {all_lines:,} lines ({gen_pct_l:.1f}%)\n\n")
        gen_rows = [
            [f"`{repo}`",
             f"{g.file_count:,}", f"{g.total_lines:,}",
             f"{g.code_lines:,}", f"{g.comment_lines:,}", f"{g.blank_lines:,}"]
            for repo, _, _, g in dir_data if g.file_count > 0
        ]
        out.write(_md_table(headers, gen_rows, aligns) + "\n\n")


def main() -> None:
    parser = argparse.ArgumentParser(description="CWT config statistics audit")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--summary", "-s", action="store_true",
                      help="Print condensed summary table instead of full report")
    mode.add_argument("--markdown", "--md", dest="markdown", action="store_true",
                      help="Write a full Markdown report document")
    parser.add_argument("--output", "-o", metavar="FILE",
                        help="Write output to FILE (for --markdown, defaults to a timestamped file)")
    args = parser.parse_args()

    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    cwt_root = os.path.join(repo_root, "cwt")
    if not os.path.isdir(cwt_root):
        print("Error: cwt/ directory not found, cannot collect statistics.")
        return

    dir_data = []
    for entry in sorted(os.scandir(cwt_root), key=lambda item: item.name):
        if not entry.is_dir():
            continue
        rel_path = os.path.join("cwt", entry.name)
        normal, generated = collect_stats(entry.path, ".cwt")
        combined = _merge_stats(normal, generated)
        dir_data.append((entry.name, rel_path, combined, generated))

    if args.markdown:
        if args.output:
            md_path = args.output
        else:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            report_dir = os.path.join(repo_root, "tmp", "reports")
            os.makedirs(report_dir, exist_ok=True)
            md_path = os.path.join(report_dir, f"config_stats_{ts}.md")
        with open(md_path, "w", encoding="utf-8") as f:
            report_markdown(dir_data, f)
        print(f"Markdown report written to: {md_path}")
    else:
        out = open(args.output, "w", encoding="utf-8") if args.output else sys.stdout
        with contextlib.redirect_stdout(out):
            if args.summary:
                report_summary(dir_data)
            else:
                report_detailed(dir_data)
        if args.output:
            out.close()
            print(f"Report written to: {args.output}")


if __name__ == "__main__":
    main()
