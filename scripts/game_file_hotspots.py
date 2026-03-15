# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

# game_file_hotspots.py — Game file distribution & hotspot audit for Paradox Language Support.
#
# Two complementary views of every locally-installed Paradox game:
#
#   1. Per-subdirectory distribution
#      Groups files by their first-level subdirectory under each entry
#      directory (e.g. common/, events/, gfx/, localisation/), then reports
#      file count and line totals per category, sorted by total lines desc.
#
#   2. Large-file hotspots
#      Lists individual files whose total line count exceeds a configurable
#      threshold (default 10000), sorted by line count descending.
#
# Both views cover script (.txt, .gfx, .gui, .asset, .lines, .dlc, .settings),
# localisation (.yml), and CSV (.csv) files — the same set tracked by the
# plugin.  Only files *indirectly* under entry directories are counted.
#
# Game metadata, entry info, and path detection logic are synced from the
# plugin's Kotlin sources (ParadoxGameType, ParadoxEntryInfo, PlsPathServiceImpl,
# PlsConstants).
#
# Auto-generated files are detected and reported separately:
#   - Path contains a '/generated/' directory segment
#   - Zero comments AND zero blank lines AND total > 1000 lines
#
# Output modes:
#   default   : full detailed report (per-subdirectory distribution + hotspots)
#   --summary : condensed summary (top-N hotspots + generated stats per game)
#   --markdown: full markdown document (saved to file)
#
# Output defaults to stdout; use --output FILE to write to a file.
# For --markdown, output defaults to a timestamped file under tmp/reports/.
#
# Usage:
#   python scripts/game_file_hotspots.py [--threshold N] [--summary] [--markdown] [--output FILE]

from __future__ import annotations

import argparse
import glob
import os
import platform
import sys
from collections import defaultdict
from dataclasses import dataclass, field
from datetime import datetime
from typing import Iterable, TextIO

# ===========================================================================
# Game metadata (synced from ParadoxGameType + ParadoxEntryInfo)
# ===========================================================================

@dataclass
class EntryInfo:
    """Mirrors ParadoxEntryInfo.  Only game-side entries are needed here."""
    game_main: list[str] = field(default_factory=list)
    game_extra: list[str] = field(default_factory=list)


@dataclass
class GameType:
    """Mirrors ParadoxGameType (game entries only, no Core)."""
    id: str
    title: str
    game_id: str
    steam_id: str
    entry_info: EntryInfo


# Entries object mirrors ParadoxGameType.Entries / EntryInfos
_COMMON_EXTRA = ["clausewitz", "jomini"]

GAME_TYPES: list[GameType] = [
    GameType("stellaris", "Stellaris", "stellaris", "281990",
             EntryInfo(game_extra=[
                 "pdx_launcher/game", "pdx_launcher/common",
                 "pdx_online_assets", "previewer_assets", "tweakergui_assets",
             ])),
    GameType("ck2", "Crusader Kings II", "ck2", "203770",
             EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("ck3", "Crusader Kings III", "ck3", "1158310",
             EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("eu4", "Europa Universalis IV", "eu4", "236850",
             EntryInfo(game_extra=_COMMON_EXTRA)),
    GameType("eu5", "Europa Universalis V", "eu5", "3450310",
             EntryInfo(
                 game_main=[
                     "game/in_game", "game/main_menu", "game/loading_screen",
                     "game/dlc/*/in_game", "game/dlc/*/main_menu", "game/dlc/*/loading_screen",
                 ],
                 game_extra=[
                     "clausewitz/main_menu", "clausewitz/loading_screen",
                     "jomini/main_menu", "jomini/loading_screen",
                 ],
             )),
    GameType("hoi4", "Hearts of Iron IV", "hoi4", "394360",
             EntryInfo(game_extra=_COMMON_EXTRA)),
    GameType("ir", "Imperator Rome", "imperator_rome", "859580",
             EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("vic2", "Victoria 2", "victoria2", "42960",
             EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("vic3", "Victoria 3", "victoria3", "529340",
             EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
]

# ===========================================================================
# File type constants (synced from PlsConstants)
# ===========================================================================

SCRIPT_EXTENSIONS = frozenset({".txt", ".gfx", ".gui", ".asset", ".lines", ".dlc", ".settings"})
LOCALISATION_EXTENSIONS = frozenset({".yml"})
CSV_EXTENSIONS = frozenset({".csv"})
ALL_EXTENSIONS = SCRIPT_EXTENSIONS | LOCALISATION_EXTENSIONS | CSV_EXTENSIONS

FILE_CATEGORIES: list[tuple[str, frozenset[str]]] = [
    ("Script", SCRIPT_EXTENSIONS),
    ("Localisation", LOCALISATION_EXTENSIONS),
    ("CSV", CSV_EXTENSIONS),
]


def file_category(ext: str) -> str | None:
    """Return the category name for a file extension, or None if not tracked."""
    for name, exts in FILE_CATEGORIES:
        if ext in exts:
            return name
    return None

# ===========================================================================
# Path detection (synced from PlsPathServiceImpl)
# ===========================================================================

_steam_path_cache: dict[str, str | None] = {}


def get_steam_path() -> str | None:
    """Get the Steam installation directory."""
    if "" in _steam_path_cache:
        return _steam_path_cache[""]
    result = _do_get_steam_path()
    _steam_path_cache[""] = result
    return result


def _do_get_steam_path() -> str | None:
    if platform.system() == "Windows":
        try:
            import winreg
            key = winreg.OpenKey(
                winreg.HKEY_LOCAL_MACHINE,
                r"SOFTWARE\WOW6432Node\Valve\Steam",
            )
            val, _ = winreg.QueryValueEx(key, "InstallPath")
            winreg.CloseKey(key)
            if val and os.path.isdir(val):
                return val
        except (FileNotFoundError, OSError):
            pass
    else:
        home = os.path.expanduser("~")
        p = os.path.join(home, ".local", "share", "Steam")
        if os.path.isdir(p):
            return p
    return None


def get_steam_game_path(steam_id: str, game_title: str) -> str | None:
    """Get a Steam game's installation directory."""
    if steam_id in _steam_path_cache:
        return _steam_path_cache[steam_id]
    result = _do_get_steam_game_path(steam_id, game_title)
    _steam_path_cache[steam_id] = result
    return result


def _do_get_steam_game_path(steam_id: str, game_title: str) -> str | None:
    if platform.system() == "Windows":
        # Try registry first
        try:
            import winreg
            key = winreg.OpenKey(
                winreg.HKEY_LOCAL_MACHINE,
                rf"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App {steam_id}",
            )
            val, _ = winreg.QueryValueEx(key, "InstallLocation")
            winreg.CloseKey(key)
            if val and os.path.isdir(val):
                return val
        except (FileNotFoundError, OSError):
            pass
    # Fallback: default Steam library path
    steam = get_steam_path()
    if steam:
        p = os.path.join(steam, "steamapps", "common", game_title)
        if os.path.isdir(p):
            return p
    return None

# ===========================================================================
# Entry directory resolution
# ===========================================================================

def resolve_entry_dirs(game_dir: str, entry_paths: list[str]) -> list[str]:
    """Resolve entry paths (possibly with * wildcards) to actual directories.

    If entry_paths is empty, the game root itself is the (sole) entry.
    """
    effective = entry_paths if entry_paths else [""]
    dirs: list[str] = []
    for ep in effective:
        if "*" in ep:
            pattern = os.path.join(game_dir, ep.replace("/", os.sep))
            for match in sorted(glob.glob(pattern)):
                if os.path.isdir(match):
                    dirs.append(match)
        else:
            full = os.path.join(game_dir, ep) if ep else game_dir
            if os.path.isdir(full):
                dirs.append(full)
    return dirs

# ===========================================================================
# Data model
# ===========================================================================

@dataclass
class FileRecord:
    """Line metrics for a single game file."""
    path: str           # absolute path
    rel_path: str       # path relative to game directory
    entry_type: str     # "Main Entry" or "Extra Entry"
    subdir: str         # first-level subdirectory under entry dir
    category: str       # "Script", "Localisation", or "CSV"
    total: int = 0
    blank: int = 0
    comment: int = 0
    generated: bool = False  # likely auto-generated file

    @property
    def code(self) -> int:
        return self.total - self.blank - self.comment


@dataclass
class SubdirStats:
    """Aggregated stats for one subdirectory."""
    file_count: int = 0
    total_lines: int = 0
    blank_lines: int = 0
    comment_lines: int = 0

    @property
    def code_lines(self) -> int:
        return self.total_lines - self.blank_lines - self.comment_lines

# ===========================================================================
# Line counting
# ===========================================================================

def count_lines(file_path: str, category: str) -> tuple[int, int, int]:
    """Return (total, blank, comment) for a game file.

    Script and localisation files use '#' for line comments.
    CSV files have no comment syntax.
    """
    total = blank = comment = 0
    has_comments = category != "CSV"

    with open(file_path, "r", encoding="utf-8-sig", errors="ignore") as f:
        for line in f:
            total += 1
            stripped = line.strip()
            if not stripped:
                blank += 1
            elif has_comments and stripped.startswith("#"):
                comment += 1

    return total, blank, comment

# ===========================================================================
# Generated-file detection
# ===========================================================================

_GAME_GEN_HEURISTIC_THRESHOLD = 1000

def is_generated_game(rel_path: str, total: int, blank: int, comment: int) -> bool:
    """Detect likely auto-generated game files.

    Heuristics:
      - Path contains a 'generated' directory segment
      - Zero comments AND zero blank lines AND total > threshold
    """
    norm = rel_path.replace(os.sep, "/")
    if "/generated/" in norm or norm.startswith("generated/"):
        return True
    if total > _GAME_GEN_HEURISTIC_THRESHOLD and comment == 0 and blank == 0:
        return True
    return False

# ===========================================================================
# File collection
# ===========================================================================

def collect_records(game_dir: str, entry_info: EntryInfo) -> list[FileRecord]:
    """Walk all entry directories and build a FileRecord for every tracked file.

    Only files *indirectly* under entry directories are collected
    (direct children of entry dirs are excluded).
    """
    records: list[FileRecord] = []

    for entry_type_label, entry_paths in [
        ("Main Entry", entry_info.game_main),
        ("Extra Entry", entry_info.game_extra),
    ]:
        dirs = resolve_entry_dirs(game_dir, entry_paths)
        for entry_dir in dirs:
            # Iterate first-level subdirectories only (skip direct children files)
            try:
                top_entries = list(os.scandir(entry_dir))
            except PermissionError:
                continue
            for item in top_entries:
                if not item.is_dir():
                    continue
                subdir_name = item.name
                for root, _, files in os.walk(item.path):
                    for fn in files:
                        ext = os.path.splitext(fn)[1].lower()
                        cat = file_category(ext)
                        if cat is None:
                            continue
                        abs_path = os.path.join(root, fn)
                        total, blank, comment = count_lines(abs_path, cat)
                        rel = os.path.relpath(abs_path, game_dir)
                        gen = is_generated_game(rel, total, blank, comment)
                        rec = FileRecord(
                            path=abs_path,
                            rel_path=rel,
                            entry_type=entry_type_label,
                            subdir=subdir_name,
                            category=cat,
                            total=total,
                            blank=blank,
                            comment=comment,
                            generated=gen,
                        )
                        records.append(rec)

    return records

# ===========================================================================
# Report: per-subdirectory distribution
# ===========================================================================

def report_subdir_distribution(out: TextIO, records: list[FileRecord]) -> None:
    """Print per-subdirectory file count and line totals, sorted by total lines."""
    # Aggregate by (entry_type, subdir)
    sd_map: dict[tuple[str, str], SubdirStats] = defaultdict(SubdirStats)
    for r in records:
        key = (r.entry_type, r.subdir)
        ss = sd_map[key]
        ss.file_count += 1
        ss.total_lines += r.total
        ss.blank_lines += r.blank
        ss.comment_lines += r.comment

    sorted_sds = sorted(sd_map.items(), key=lambda kv: kv[1].total_lines, reverse=True)

    out.write("\n  --- Per-Subdirectory Distribution ---\n\n")
    out.write(f"  {'Entry':<12} {'Subdirectory':<36} {'Files':>6} {'Total':>9} {'Code':>9} {'Cmt':>7} {'Blk':>7}\n")
    out.write(f"  {'-'*12} {'-'*36} {'-'*6} {'-'*9} {'-'*9} {'-'*7} {'-'*7}\n")
    for (entry_type, subdir), ss in sorted_sds:
        et_short = "Main" if "Main" in entry_type else "Extra"
        display_sd = subdir if len(subdir) <= 36 else "..." + subdir[-(36-3):]
        out.write(f"  {et_short:<12} {display_sd:<36} {ss.file_count:>6} {ss.total_lines:>9} {ss.code_lines:>9} {ss.comment_lines:>7} {ss.blank_lines:>7}\n")

    # Summary
    total_files = sum(ss.file_count for ss in sd_map.values())
    total_lines = sum(ss.total_lines for ss in sd_map.values())
    total_code  = sum(ss.code_lines for ss in sd_map.values())
    total_cmt   = sum(ss.comment_lines for ss in sd_map.values())
    total_blank = sum(ss.blank_lines for ss in sd_map.values())
    sd_count    = len(sd_map)
    out.write(f"  {'-'*12} {'-'*36} {'-'*6} {'-'*9} {'-'*9} {'-'*7} {'-'*7}\n")
    out.write(f"  {'TOTAL':<12} {f'{sd_count} subdirectories':<36} {total_files:>6} {total_lines:>9} {total_code:>9} {total_cmt:>7} {total_blank:>7}\n")

# ===========================================================================
# Report: per-subdirectory distribution by category
# ===========================================================================

def report_subdir_by_category(out: TextIO, records: list[FileRecord]) -> None:
    """Print per-subdirectory breakdown split by file category."""
    # Aggregate by (subdir, category)
    sd_cat_map: dict[tuple[str, str], SubdirStats] = defaultdict(SubdirStats)
    for r in records:
        key = (r.subdir, r.category)
        ss = sd_cat_map[key]
        ss.file_count += 1
        ss.total_lines += r.total
        ss.blank_lines += r.blank
        ss.comment_lines += r.comment

    # Get subdirs sorted by total lines
    subdir_totals: dict[str, int] = defaultdict(int)
    for (sd, _), ss in sd_cat_map.items():
        subdir_totals[sd] += ss.total_lines
    sorted_sds = sorted(subdir_totals.keys(), key=lambda sd: subdir_totals[sd], reverse=True)

    # Only show top 30 subdirectories to keep the report manageable
    top_n = 30
    shown = sorted_sds[:top_n]

    out.write(f"\n  --- Top {min(top_n, len(shown))} Subdirectories by Category ---\n\n")
    out.write(f"  {'Subdirectory':<30} {'Category':<14} {'Files':>6} {'Total':>9} {'Code':>9} {'Cmt':>7} {'Blk':>7}\n")
    out.write(f"  {'-'*30} {'-'*14} {'-'*6} {'-'*9} {'-'*9} {'-'*7} {'-'*7}\n")
    for sd in shown:
        first = True
        for cat, _ in FILE_CATEGORIES:
            ss = sd_cat_map.get((sd, cat))
            if ss is None or ss.file_count == 0:
                continue
            display_sd = sd if first else ""
            if first and len(sd) > 30:
                display_sd = "..." + sd[-(30-3):]
            out.write(f"  {display_sd:<30} {cat:<14} {ss.file_count:>6} {ss.total_lines:>9} {ss.code_lines:>9} {ss.comment_lines:>7} {ss.blank_lines:>7}\n")
            first = False
        if not first:
            out.write("\n")

# ===========================================================================
# Report: large-file hotspots
# ===========================================================================

DEFAULT_THRESHOLD = 10000

def report_hotspots(out: TextIO, records: list[FileRecord], threshold: int) -> None:
    """Print files exceeding the line threshold, sorted by total lines."""
    hot = [r for r in records if r.total >= threshold]
    hot.sort(key=lambda r: r.total, reverse=True)

    out.write(f"\n  --- Large-File Hotspots (>= {threshold} lines) ---\n\n")

    if not hot:
        out.write(f"  No files found with >= {threshold} lines.\n")
        return

    out.write(f"  {'#':>4} {'Lines':>8} {'Code':>8} {'Cmt':>6} {'Blk':>6}  {'':>5} {'Cat':<14} {'File'}\n")
    out.write(f"  {'-'*4} {'-'*8} {'-'*8} {'-'*6} {'-'*6}  {'-'*5} {'-'*14} {'-'*60}\n")
    for i, r in enumerate(hot, 1):
        tag = "[GEN]" if r.generated else ""
        out.write(f"  {i:>4} {r.total:>8} {r.code:>8} {r.comment:>6} {r.blank:>6}  {tag:>5} {r.category:<14} {r.rel_path}\n")

    out.write(f"\n  Total hotspot files: {len(hot)} / {len(records)}  ({len(hot)/len(records)*100:.1f}%)\n")
    hotspot_lines = sum(r.total for r in hot)
    all_lines = sum(r.total for r in records)
    pct = hotspot_lines / all_lines * 100 if all_lines else 0.0
    out.write(f"  Hotspot total lines: {hotspot_lines} / {all_lines}  ({pct:.1f}%)\n")

# ===========================================================================
# Report: generated file summary
# ===========================================================================

def report_generated_summary(out: TextIO, records: list[FileRecord]) -> None:
    """Print a summary of auto-detected generated files."""
    gen = [r for r in records if r.generated]
    if not gen:
        return

    gen.sort(key=lambda r: r.total, reverse=True)
    all_lines = sum(r.total for r in records)
    gen_lines = sum(r.total for r in gen)
    pct = gen_lines / all_lines * 100 if all_lines else 0.0

    out.write(f"\n  --- Generated File Summary ---\n\n")
    out.write(f"  Generated files : {len(gen)} / {len(records)}  ({len(gen)/len(records)*100:.1f}%)\n")
    out.write(f"  Generated lines : {gen_lines} / {all_lines}  ({pct:.1f}%)\n")
    out.write(f"\n")
    out.write(f"  {'#':>4} {'Lines':>8} {'Code':>8} {'Cmt':>6} {'Blk':>6}  {'Cat':<14} {'File'}\n")
    out.write(f"  {'-'*4} {'-'*8} {'-'*8} {'-'*6} {'-'*6}  {'-'*14} {'-'*60}\n")
    for i, r in enumerate(gen, 1):
        out.write(f"  {i:>4} {r.total:>8} {r.code:>8} {r.comment:>6} {r.blank:>6}  {r.category:<14} {r.rel_path}\n")

# ===========================================================================
# Report: markdown
# ===========================================================================

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


def report_markdown(out: TextIO, game_data: list, threshold: int) -> None:
    """Write a full markdown report document."""
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    out.write("# Game File Distribution & Hotspot Report\n\n")
    out.write(f"> Generated: {ts}\n\n")

    if not game_data:
        out.write("*No locally installed Paradox games detected.*\n")
        return

    # Summary table
    out.write("## Summary\n\n")
    s_hdrs = ["Game", "Files", "Lines", "Gen Files", "Gen Lines", "Gen%",
              f"Hotspots (\u2265{threshold:,})"]
    s_aln  = [":---", "---:", "---:", "---:", "---:", "---:", "---:"]
    s_rows = []
    for gt, game_dir, records in game_data:
        tf = len(records)
        tl = sum(r.total for r in records)
        gen = [r for r in records if r.generated]
        gl  = sum(r.total for r in gen)
        hot = sum(1 for r in records if r.total >= threshold)
        s_rows.append([gt.title, f"{tf:,}", f"{tl:,}",
                       f"{len(gen):,}", f"{gl:,}",
                       f"{gl/tl*100:.1f}%" if tl else "0.0%",
                       f"{hot:,}"])
    out.write(_md_table(s_hdrs, s_rows, s_aln) + "\n\n")
    out.write(f"Detected {len(game_data)} / {len(GAME_TYPES)} game(s).\n\n")

    # Per-game sections
    for gt, game_dir, records in game_data:
        tf = len(records)
        tl = sum(r.total for r in records)
        gen = [r for r in records if r.generated]
        gen_lines = sum(r.total for r in gen)

        out.write(f"## {gt.title}\n\n")
        out.write(f"Directory: `{game_dir}`  \n")
        out.write(f"Files: **{tf:,}** total, **{tl:,}** lines")
        if gen:
            out.write(f"  \nGenerated: **{len(gen):,}** files "
                      f"({len(gen)/tf*100:.1f}%), "
                      f"**{gen_lines:,}** lines ({gen_lines/tl*100:.1f}%)")
        out.write("\n\n")

        # Hotspots
        hot = [r for r in records if r.total >= threshold]
        hot.sort(key=lambda r: r.total, reverse=True)
        out.write(f"### Large-File Hotspots (\u2265 {threshold:,} lines)\n\n")
        if not hot:
            out.write(f"*No files with \u2265 {threshold:,} lines.*\n\n")
        else:
            headers = ["#", "Lines", "Code", "Comment", "Blank", "Tag", "Category", "File"]
            aligns  = ["---:", "---:", "---:", "---:", "---:", ":---", ":---", ":---"]
            rows = []
            for i, r in enumerate(hot, 1):
                tag = "GEN" if r.generated else ""
                rows.append([i, f"{r.total:,}", f"{r.code:,}", f"{r.comment:,}",
                             f"{r.blank:,}", tag, r.category, f"`{r.rel_path}`"])
            out.write(_md_table(headers, rows, aligns) + "\n\n")
            hl = sum(r.total for r in hot)
            out.write(f"Hotspot files: **{len(hot):,}** / {tf:,} "
                      f"({len(hot)/tf*100:.1f}%) \u2014 "
                      f"{hl:,} / {tl:,} lines ({hl/tl*100:.1f}%)\n\n")

        # Generated files
        if gen:
            gen_s = sorted(gen, key=lambda r: r.total, reverse=True)
            out.write("### Generated Files\n\n")
            headers = ["#", "Lines", "Code", "Comment", "Blank", "Category", "File"]
            aligns  = ["---:", "---:", "---:", "---:", "---:", ":---", ":---"]
            rows = [[i, f"{r.total:,}", f"{r.code:,}", f"{r.comment:,}",
                     f"{r.blank:,}", r.category, f"`{r.rel_path}`"]
                    for i, r in enumerate(gen_s, 1)]
            out.write(_md_table(headers, rows, aligns) + "\n\n")

# ===========================================================================
# Report: detailed (full)
# ===========================================================================

def report_detailed(out: TextIO, game_data: list, threshold: int) -> None:
    """Full detailed report: per-subdirectory distribution + hotspots + generated summary."""
    out.write("=== Game File Distribution & Hotspot Report ===\n")
    if not game_data:
        out.write("\nNo locally installed Paradox games detected.\n")
        return
    for gt, game_dir, records in game_data:
        out.write(f"\n{'='*80}\n")
        out.write(f"Game: {gt.title} ({gt.id})\n")
        out.write(f"Directory: {game_dir}\n")
        report_subdir_distribution(out, records)
        report_subdir_by_category(out, records)
        report_hotspots(out, records, threshold)
        report_generated_summary(out, records)
    out.write(f"\n{'='*80}\n")
    out.write(f"Detected {len(game_data)} / {len(GAME_TYPES)} game(s).\n")

# ===========================================================================
# Report: summary (condensed)
# ===========================================================================

_SUMMARY_TOP_N = 10

def report_summary(out: TextIO, game_data: list, threshold: int) -> None:
    """Condensed summary: top-N hotspots and generated stats per game."""
    out.write("=== Game File Hotspot Summary ===\n")
    if not game_data:
        out.write("\nNo locally installed Paradox games detected.\n")
        return
    for gt, game_dir, records in game_data:
        total_files = len(records)
        total_lines = sum(r.total for r in records)
        gen = [r for r in records if r.generated]
        gen_lines = sum(r.total for r in gen)
        gen_file_pct = len(gen) / total_files * 100 if total_files else 0.0
        gen_line_pct = gen_lines / total_lines * 100 if total_lines else 0.0

        out.write(f"\n[{gt.title}]\n")
        out.write(f"  Files  : {total_files:,} total, {total_lines:,} lines\n")
        if gen:
            out.write(f"  Generated: {len(gen)} files ({gen_file_pct:.1f}%), {gen_lines:,} lines ({gen_line_pct:.1f}%)\n")

        hot = sorted(records, key=lambda r: r.total, reverse=True)[:_SUMMARY_TOP_N]
        if hot:
            out.write(f"  Top {_SUMMARY_TOP_N} files:\n")
            out.write(f"    {'#':>4} {'Lines':>9}  {'':>5} {'Cat':<14} {'File'}\n")
            out.write(f"    {'-'*4} {'-'*9}  {'-'*5} {'-'*14} {'-'*50}\n")
            for i, r in enumerate(hot, 1):
                tag = "[GEN]" if r.generated else ""
                out.write(f"    {i:>4} {r.total:>9}  {tag:>5} {r.category:<14} {r.rel_path}\n")

    out.write(f"\nDetected {len(game_data)} / {len(GAME_TYPES)} game(s).\n")

# ===========================================================================
# Entry point
# ===========================================================================

def main() -> None:
    parser = argparse.ArgumentParser(description="Game file distribution & hotspot audit")
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

    # Collect per-game data (progress to stderr so it doesn't pollute stdout)
    game_data = []
    for gt in GAME_TYPES:
        game_dir = get_steam_game_path(gt.steam_id, gt.title)
        if game_dir is None or not os.path.isdir(game_dir):
            continue
        print(f"  Scanning {gt.title}...", file=sys.stderr)
        records = collect_records(game_dir, gt.entry_info)
        game_data.append((gt, game_dir, records))

    if args.markdown:
        if args.output:
            md_path = args.output
        else:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
            report_dir = os.path.join(repo_root, "tmp", "reports")
            os.makedirs(report_dir, exist_ok=True)
            md_path = os.path.join(report_dir, f"game_file_hotspots_{ts}.md")
        with open(md_path, "w", encoding="utf-8") as f:
            report_markdown(f, game_data, args.threshold)
        print(f"Markdown report written to: {md_path}", file=sys.stderr)
    else:
        if args.output:
            out = open(args.output, "w", encoding="utf-8")
        else:
            out = sys.stdout
        try:
            if args.summary:
                report_summary(out, game_data, args.threshold)
            else:
                report_detailed(out, game_data, args.threshold)
        finally:
            if args.output:
                out.close()
                print(f"\nReport written to: {args.output}", file=sys.stderr)

    print(f"Detected {len(game_data)} game(s).", file=sys.stderr)


if __name__ == "__main__":
    main()
