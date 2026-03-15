# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

# config_hotspots.py — CWT config distribution & hotspot audit for Paradox Language Support.
#
# Two complementary views of the CWT config repositories:
#
#   1. Per-subdirectory distribution
#      Groups .cwt files by their immediate subdirectory under each config
#      repository (e.g. config/common/buildings), then reports file count
#      and line totals, sorted by total lines descending.
#
#   2. Large-file hotspots
#      Lists individual .cwt files whose total line count exceeds a
#      configurable threshold (default 1000), sorted by line count descending.
#
# CWT comment classification (same rules as config_stats.py):
#   - '#'   (single)  : line comment   -> counted as comment
#   - '##'  (exactly 2): option comment -> counted as code (metadata)
#   - '###' (3 or more): doc comment   -> counted as comment
#
# Auto-generated files are detected and reported separately:
#   - Filename contains '.gen.' (e.g. modifiers.gen.cwt)
#   - Zero comments AND zero blank lines AND total > 500 lines
#
# Output modes:
#   default   : per-subdirectory distribution + large-file hotspots + generated summary
#   --summary : condensed top-N hotspot list + generated stats only
#   --markdown: full markdown document (saved to file)
#
# Output defaults to stdout; use --output FILE to write to a file.
# For --markdown, output defaults to a timestamped file under tmp/reports/.
#
# Usage:
#   python scripts/config_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]

from __future__ import annotations

import argparse
import contextlib
import os
import sys
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime
from typing import Iterable

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

DEFAULT_THRESHOLD = 1000

# ---------------------------------------------------------------------------
# Data model
# ---------------------------------------------------------------------------

@dataclass
class FileRecord:
    """Line metrics for a single CWT config file."""
    path: str          # absolute path
    rel_path: str      # path relative to repo root
    repo: str          # config repository name (e.g. cwtools-stellaris-config)
    subdir: str        # subdirectory within repo (e.g. config/common/buildings)
    total: int = 0
    blank: int = 0
    comment: int = 0
    generated: bool = False  # likely auto-generated file

    @property
    def code(self) -> int:
        return self.total - self.blank - self.comment


@dataclass
class SubdirStats:
    """Aggregated stats for one subdirectory across all its files."""
    file_count: int = 0
    total_lines: int = 0
    blank_lines: int = 0
    comment_lines: int = 0

    @property
    def code_lines(self) -> int:
        return self.total_lines - self.blank_lines - self.comment_lines

# ---------------------------------------------------------------------------
# Line counting (CWT comment-aware)
# ---------------------------------------------------------------------------

def count_lines(file_path: str) -> tuple[int, int, int]:
    """Return (total, blank, comment) line counts for a CWT file.

    Comment classification:
      - '###'+  : doc comment   -> comment
      - '##'    : option comment -> code (metadata)
      - '#'     : line comment  -> comment
    """
    total = 0
    blank = 0
    comment = 0

    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            total += 1
            stripped = line.strip()

            if not stripped:
                blank += 1
                continue

            if stripped.startswith("#"):
                hashes = 0
                for ch in stripped:
                    if ch == "#":
                        hashes += 1
                    else:
                        break

                if hashes >= 3:
                    # Doc comment -> comment
                    comment += 1
                elif hashes == 2:
                    # Option comment -> code (not counted as comment)
                    pass
                else:
                    # Line comment -> comment
                    comment += 1
                continue

    return total, blank, comment

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
# File discovery
# ---------------------------------------------------------------------------

def derive_subdir(file_path: str, repo_root: str) -> str:
    """Derive the subdirectory path relative to the repo root."""
    rel = os.path.relpath(os.path.dirname(file_path), repo_root)
    if rel == ".":
        return "(root)"
    return rel.replace(os.sep, "/")


def collect_records(cwt_root: str, repo_root: str) -> list[FileRecord]:
    """Walk all CWT config repositories and build a FileRecord for every .cwt file."""
    records: list[FileRecord] = []
    for entry in sorted(os.scandir(cwt_root), key=lambda e: e.name):
        if not entry.is_dir():
            continue
        repo_name = entry.name
        for dirpath, _, filenames in os.walk(entry.path):
            for fn in filenames:
                if not fn.endswith(".cwt"):
                    continue
                abs_path = os.path.join(dirpath, fn)
                total, blank, comment = count_lines(abs_path)
                gen = is_generated_cwt(fn, total, blank, comment)
                rec = FileRecord(
                    path=abs_path,
                    rel_path=os.path.relpath(abs_path, repo_root),
                    repo=repo_name,
                    subdir=derive_subdir(abs_path, entry.path),
                    total=total,
                    blank=blank,
                    comment=comment,
                    generated=gen,
                )
                records.append(rec)
    return records

# ---------------------------------------------------------------------------
# Report: per-subdirectory distribution
# ---------------------------------------------------------------------------

def report_subdir_distribution(records: list[FileRecord]) -> None:
    """Print per-subdirectory file count and line totals, sorted by total lines."""
    # Aggregate by (repo, subdir)
    sd_map: dict[tuple[str, str], SubdirStats] = defaultdict(SubdirStats)
    for r in records:
        key = (r.repo, r.subdir)
        ss = sd_map[key]
        ss.file_count += 1
        ss.total_lines += r.total
        ss.blank_lines += r.blank
        ss.comment_lines += r.comment

    sorted_sds = sorted(sd_map.items(), key=lambda kv: kv[1].total_lines, reverse=True)

    print("=== Per-Subdirectory Config Distribution ===")
    print()
    # Header
    print(f"  {'Repository':<28} {'Subdirectory':<40} {'Files':>5} {'Total':>7} {'Code':>7} {'Cmt':>5} {'Blk':>5}")
    print(f"  {'-'*28} {'-'*40} {'-'*5} {'-'*7} {'-'*7} {'-'*5} {'-'*5}")
    for (repo, subdir), ss in sorted_sds:
        # Abbreviate repo name for display
        display_repo = repo.replace("cwtools-", "").replace("-config", "")
        display_sd = subdir if len(subdir) <= 40 else "..." + subdir[-(40-3):]
        print(f"  {display_repo:<28} {display_sd:<40} {ss.file_count:>5} {ss.total_lines:>7} {ss.code_lines:>7} {ss.comment_lines:>5} {ss.blank_lines:>5}")

    # Summary
    total_files = sum(ss.file_count for ss in sd_map.values())
    total_lines = sum(ss.total_lines for ss in sd_map.values())
    total_code  = sum(ss.code_lines for ss in sd_map.values())
    total_cmt   = sum(ss.comment_lines for ss in sd_map.values())
    total_blank = sum(ss.blank_lines for ss in sd_map.values())
    sd_count    = len(sd_map)
    print(f"  {'-'*28} {'-'*40} {'-'*5} {'-'*7} {'-'*7} {'-'*5} {'-'*5}")
    print(f"  {'TOTAL':<28} {f'{sd_count} subdirectories':<40} {total_files:>5} {total_lines:>7} {total_code:>7} {total_cmt:>5} {total_blank:>5}")

# ---------------------------------------------------------------------------
# Report: large-file hotspots
# ---------------------------------------------------------------------------

def report_hotspots(records: list[FileRecord], threshold: int) -> None:
    """Print .cwt files exceeding the line threshold, sorted by total lines."""
    hot = [r for r in records if r.total >= threshold]
    hot.sort(key=lambda r: r.total, reverse=True)

    print()
    print(f"=== Large-File Hotspots (>= {threshold} lines) ===")
    print()

    if not hot:
        print(f"  No files found with >= {threshold} lines.")
        return

    print(f"  {'#':>4} {'Lines':>6} {'Code':>6} {'Cmt':>5} {'Blk':>5}  {'':>5} {'File'}")
    print(f"  {'-'*4} {'-'*6} {'-'*6} {'-'*5} {'-'*5}  {'-'*5} {'-'*60}")
    for i, r in enumerate(hot, 1):
        tag = "[GEN]" if r.generated else ""
        print(f"  {i:>4} {r.total:>6} {r.code:>6} {r.comment:>5} {r.blank:>5}  {tag:>5} {r.rel_path}")

    print()
    print(f"  Total hotspot files: {len(hot)} / {len(records)}  ({len(hot)/len(records)*100:.1f}%)")
    hotspot_lines = sum(r.total for r in hot)
    all_lines = sum(r.total for r in records)
    print(f"  Hotspot total lines: {hotspot_lines} / {all_lines}  ({hotspot_lines/all_lines*100:.1f}%)")

# ---------------------------------------------------------------------------
# Report: generated file summary
# ---------------------------------------------------------------------------

def report_generated_summary(records: list[FileRecord]) -> None:
    """Print a summary of auto-detected generated files."""
    gen = [r for r in records if r.generated]
    if not gen:
        return

    gen.sort(key=lambda r: r.total, reverse=True)
    all_lines = sum(r.total for r in records)
    gen_lines = sum(r.total for r in gen)
    pct = gen_lines / all_lines * 100 if all_lines else 0.0

    print()
    print(f"=== Generated File Summary ===")
    print()
    print(f"  Generated files : {len(gen)} / {len(records)}  ({len(gen)/len(records)*100:.1f}%)")
    print(f"  Generated lines : {gen_lines} / {all_lines}  ({pct:.1f}%)")
    print()
    print(f"  {'#':>4} {'Lines':>6} {'Code':>6} {'Cmt':>5} {'Blk':>5}  {'File'}")
    print(f"  {'-'*4} {'-'*6} {'-'*6} {'-'*5} {'-'*5}  {'-'*60}")
    for i, r in enumerate(gen, 1):
        print(f"  {i:>4} {r.total:>6} {r.code:>6} {r.comment:>5} {r.blank:>5}  {r.rel_path}")

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
    out.write("# CWT Config Distribution & Hotspot Report\n\n")
    out.write(f"> Generated: {ts}\n\n")

    # Per-subdirectory distribution
    sd_map: dict[tuple[str, str], SubdirStats] = defaultdict(SubdirStats)
    for r in records:
        key = (r.repo, r.subdir)
        ss = sd_map[key]
        ss.file_count += 1
        ss.total_lines += r.total
        ss.blank_lines += r.blank
        ss.comment_lines += r.comment
    sorted_sds = sorted(sd_map.items(), key=lambda kv: kv[1].total_lines, reverse=True)

    out.write("## Per-Subdirectory Distribution\n\n")
    headers = ["Repository", "Subdirectory", "Files", "Total", "Code", "Comment", "Blank"]
    aligns  = [":---", ":---", "---:", "---:", "---:", "---:", "---:"]
    rows = []
    for (repo, subdir), ss in sorted_sds:
        rows.append([repo, subdir if subdir else "(root)",
                     f"{ss.file_count:,}", f"{ss.total_lines:,}",
                     f"{ss.code_lines:,}", f"{ss.comment_lines:,}", f"{ss.blank_lines:,}"])
    total_f = sum(ss.file_count for ss in sd_map.values())
    total_l = sum(ss.total_lines for ss in sd_map.values())
    total_c = sum(ss.code_lines  for ss in sd_map.values())
    total_cmt = sum(ss.comment_lines for ss in sd_map.values())
    total_b = sum(ss.blank_lines  for ss in sd_map.values())
    rows.append(["**TOTAL**", f"**{len(sd_map)} subdirectories**",
                 f"**{total_f:,}**", f"**{total_l:,}**",
                 f"**{total_c:,}**", f"**{total_cmt:,}**", f"**{total_b:,}**"])
    out.write(_md_table(headers, rows, aligns) + "\n\n")

    # Hotspots
    hot = [r for r in records if r.total >= threshold]
    hot.sort(key=lambda r: r.total, reverse=True)
    out.write(f"## Large-File Hotspots (\u2265 {threshold} lines)\n\n")
    if not hot:
        out.write(f"*No files found with \u2265 {threshold} lines.*\n\n")
    else:
        headers = ["#", "Lines", "Code", "Comment", "Blank", "Tag", "File"]
        aligns  = ["---:", "---:", "---:", "---:", "---:", ":---", ":---"]
        rows = []
        for i, r in enumerate(hot, 1):
            tag = "GEN" if r.generated else ""
            rows.append([i, f"{r.total:,}", f"{r.code:,}", f"{r.comment:,}",
                         f"{r.blank:,}", tag, f"`{r.rel_path}`"])
        out.write(_md_table(headers, rows, aligns) + "\n\n")
        hotspot_lines = sum(r.total for r in hot)
        all_lines     = sum(r.total for r in records)
        out.write(f"Hotspot files: **{len(hot):,}** / {len(records):,} "
                  f"({len(hot)/len(records)*100:.1f}%) "
                  f"\u2014 {hotspot_lines:,} / {all_lines:,} lines "
                  f"({hotspot_lines/all_lines*100:.1f}%)\n\n")

    # Generated files
    gen = [r for r in records if r.generated]
    if gen:
        gen.sort(key=lambda r: r.total, reverse=True)
        all_lines = sum(r.total for r in records)
        gen_lines = sum(r.total for r in gen)
        gen_pct_f = len(gen) / len(records) * 100 if records else 0.0
        gen_pct_l = gen_lines / all_lines * 100 if all_lines else 0.0
        out.write("## Generated Files\n\n")
        out.write(f"Auto-detected: **{len(gen)}** / {len(records):,} files ({gen_pct_f:.1f}%), "
                  f"**{gen_lines:,}** / {all_lines:,} lines ({gen_pct_l:.1f}%)\n\n")
        headers = ["#", "Lines", "Code", "Comment", "Blank", "File"]
        aligns  = ["---:", "---:", "---:", "---:", "---:", ":---"]
        rows = [[i, f"{r.total:,}", f"{r.code:,}", f"{r.comment:,}",
                 f"{r.blank:,}", f"`{r.rel_path}`"]
                for i, r in enumerate(gen, 1)]
        out.write(_md_table(headers, rows, aligns) + "\n\n")

# ---------------------------------------------------------------------------
# Report: summary (condensed)
# ---------------------------------------------------------------------------

_SUMMARY_TOP_N = 10

def report_summary(records: list[FileRecord], threshold: int) -> None:
    """Condensed summary: top-N hotspot list and generated stats."""
    hot = sorted(records, key=lambda r: r.total, reverse=True)[:_SUMMARY_TOP_N]
    gen = [r for r in records if r.generated]
    all_lines = sum(r.total for r in records)
    gen_lines = sum(r.total for r in gen)

    print()
    print(f"=== CWT Config Hotspot Summary (Top {_SUMMARY_TOP_N}) ===")
    print()
    print(f"  {'#':>4} {'Lines':>6} {'Code':>6} {'Cmt':>5} {'Blk':>5}  {'':>5} {'File'}")
    print(f"  {'-'*4} {'-'*6} {'-'*6} {'-'*5} {'-'*5}  {'-'*5} {'-'*60}")
    for i, r in enumerate(hot, 1):
        tag = "[GEN]" if r.generated else ""
        print(f"  {i:>4} {r.total:>6} {r.code:>6} {r.comment:>5} {r.blank:>5}  {tag:>5} {r.rel_path}")

    print()
    if gen:
        gen_pct_f = len(gen) / len(records) * 100 if records else 0.0
        gen_pct_l = gen_lines / all_lines * 100 if all_lines else 0.0
        print(f"  Generated: {len(gen)} / {len(records)} files ({gen_pct_f:.1f}%), {gen_lines:,} / {all_lines:,} lines ({gen_pct_l:.1f}%)")
    else:
        print(f"  No generated files detected.")
    print(f"  Total files: {len(records)}, total lines: {all_lines:,}")

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(description="CWT config distribution & hotspot audit")
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
    cwt_root = os.path.join(repo_root, "cwt")
    if not os.path.isdir(cwt_root):
        print("Error: cwt/ directory not found, cannot collect statistics.")
        return

    records = collect_records(cwt_root, repo_root)

    if args.markdown:
        if args.output:
            md_path = args.output
        else:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            report_dir = os.path.join(repo_root, "tmp", "reports")
            os.makedirs(report_dir, exist_ok=True)
            md_path = os.path.join(report_dir, f"config_hotspots_{ts}.md")
        with open(md_path, "w", encoding="utf-8") as f:
            report_markdown(records, args.threshold, f)
        print(f"Markdown report written to: {md_path}")
    else:
        out = open(args.output, "w", encoding="utf-8") if args.output else sys.stdout
        with contextlib.redirect_stdout(out):
            if args.summary:
                report_summary(records, args.threshold)
            else:
                report_subdir_distribution(records)
                report_hotspots(records, args.threshold)
                report_generated_summary(records)
        if args.output:
            out.close()
            print(f"Report written to: {args.output}")


if __name__ == "__main__":
    main()
