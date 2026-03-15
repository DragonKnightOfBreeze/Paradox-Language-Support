# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

# game_file_stats.py — Game file statistics audit for Paradox Language Support.
#
# Detects locally installed Paradox games via Steam registry / default paths,
# then walks each game's entry directories to count script, localisation, and
# CSV files.  Reports per-game, per-entry-type (main / extra), per-category
# line metrics: total, min, max, avg, blank, comment, and code lines.
#
# Key design decisions (synced from plugin Kotlin sources):
#   - Game types, entry info, and path detection logic mirror
#     ParadoxGameType, ParadoxEntryInfo, and PlsPathServiceImpl.
#   - File extensions mirror PlsConstants.
#   - Only files *indirectly* under entry directories are counted
#     (files directly in an entry dir, like license.txt, are excluded).
#   - Option comments (##) in CWT are N/A here; Paradox script and
#     localisation use plain '#' line comments.  CSV has no comments.
#
# Auto-generated files are detected and reported separately:
#   - Path contains a '/generated/' directory segment
#   - Zero comments AND zero blank lines AND total > 1000 lines
#
# Output modes:
#   default  : full detailed report (per-entry-type breakdown + combined + generated)
#   --summary: condensed per-game table with file/line totals and generated percentages
#
# Output defaults to stdout; use --output FILE to write to a file.
#
# Usage:
#   python scripts/game_file_stats.py [--output FILE] [--summary]

from __future__ import annotations

import argparse
import glob
import os
import platform
import sys
from collections import defaultdict
from dataclasses import dataclass, field
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
            # Glob-expand wildcard entry paths (e.g. game/dlc/*/in_game)
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
class Stats:
    """Aggregated line statistics."""
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
        return self.total_lines / self.file_count if self.file_count else 0.0

    def add_file(self, total: int, blank: int, comment: int) -> None:
        self.file_count += 1
        self.total_lines += total
        self.blank_lines += blank
        self.comment_lines += comment
        if self.file_count == 1:
            self.min_lines = total
            self.max_lines = total
        else:
            self.min_lines = min(self.min_lines, total)
            self.max_lines = max(self.max_lines, total)

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

def iter_indirect_files(entry_dir: str) -> Iterable[str]:
    """Yield files that are INDIRECTLY under entry_dir (in subdirectories only).

    Files directly in entry_dir (e.g. license.txt) are excluded.
    """
    try:
        entries = os.scandir(entry_dir)
    except PermissionError:
        return
    for item in entries:
        if item.is_dir():
            for root, _, files in os.walk(item.path):
                for fn in files:
                    yield os.path.join(root, fn)


def collect_game_stats(game_dir: str, entry_info: EntryInfo
                       ) -> tuple[dict[tuple[str, str], Stats],
                                  dict[tuple[str, str], Stats]]:
    """Collect stats keyed by (entry_type, category).

    entry_type is "Main Entry" or "Extra Entry".
    category is "Script", "Localisation", or "CSV".

    Returns (normal_stats_map, generated_stats_map).
    """
    normal: dict[tuple[str, str], Stats] = defaultdict(Stats)
    generated: dict[tuple[str, str], Stats] = defaultdict(Stats)

    for entry_type_label, entry_paths in [
        ("Main Entry", entry_info.game_main),
        ("Extra Entry", entry_info.game_extra),
    ]:
        dirs = resolve_entry_dirs(game_dir, entry_paths)
        for entry_dir in dirs:
            for file_path in iter_indirect_files(entry_dir):
                ext = os.path.splitext(file_path)[1].lower()
                cat = file_category(ext)
                if cat is None:
                    continue
                total, blank, comment = count_lines(file_path, cat)
                rel = os.path.relpath(file_path, game_dir)
                target = generated if is_generated_game(rel, total, blank, comment) else normal
                target[(entry_type_label, cat)].add_file(total, blank, comment)

    return normal, generated

# ===========================================================================
# Reporting
# ===========================================================================

def print_stats(out: TextIO, label: str, stats: Stats) -> None:
    """Print a formatted block for one category."""
    out.write(f"    [{label}]\n")
    out.write(f"      Files        : {stats.file_count}\n")
    if stats.file_count == 0:
        return
    out.write(f"      Total lines  : {stats.total_lines}\n")
    out.write(f"      Min lines    : {stats.min_lines}\n")
    out.write(f"      Max lines    : {stats.max_lines}\n")
    out.write(f"      Avg lines    : {stats.avg_lines:.1f}\n")
    out.write(f"      Blank lines  : {stats.blank_lines}\n")
    out.write(f"      Comment lines: {stats.comment_lines}\n")
    out.write(f"      Code lines   : {stats.code_lines}\n")


def print_combined_stats(out: TextIO, label: str,
                         stats_map: dict[tuple[str, str], Stats]) -> None:
    """Print a combined (all entries) summary for one category."""
    combined = Stats()
    for (_, cat), s in stats_map.items():
        if cat != label:
            continue
        combined.file_count += s.file_count
        combined.total_lines += s.total_lines
        combined.blank_lines += s.blank_lines
        combined.comment_lines += s.comment_lines
        if combined.file_count == s.file_count:
            combined.min_lines = s.min_lines
            combined.max_lines = s.max_lines
        else:
            if s.file_count > 0:
                combined.min_lines = min(combined.min_lines, s.min_lines)
                combined.max_lines = max(combined.max_lines, s.max_lines)
    print_stats(out, label, combined)


def _merge_stats_maps(*maps: dict[tuple[str, str], Stats]) -> dict[tuple[str, str], Stats]:
    """Merge multiple stats maps into one combined map."""
    result: dict[tuple[str, str], Stats] = defaultdict(Stats)
    for m in maps:
        for key, s in m.items():
            r = result[key]
            r.file_count += s.file_count
            r.total_lines += s.total_lines
            r.blank_lines += s.blank_lines
            r.comment_lines += s.comment_lines
            if r.file_count == s.file_count:
                r.min_lines = s.min_lines
                r.max_lines = s.max_lines
            else:
                if s.file_count > 0:
                    r.min_lines = min(r.min_lines, s.min_lines)
                    r.max_lines = max(r.max_lines, s.max_lines)
    return result


def _has_any_files(stats_map: dict[tuple[str, str], Stats]) -> bool:
    """Check if any Stats in the map has file_count > 0."""
    return any(s.file_count > 0 for s in stats_map.values())


def _total_files(stats_map: dict[tuple[str, str], Stats]) -> int:
    return sum(s.file_count for s in stats_map.values())


def _total_lines(stats_map: dict[tuple[str, str], Stats]) -> int:
    return sum(s.total_lines for s in stats_map.values())

# ===========================================================================
# Entry point
# ===========================================================================

def report_detailed(out: TextIO, game_data: list) -> None:
    """Full detailed report: per-entry-type breakdown + combined + generated split."""
    out.write("=== Game File Statistics Report ===\n")
    if not game_data:
        out.write("\nNo locally installed Paradox games detected.\n")
        return
    for gt, game_dir, normal_map, gen_map in game_data:
        stats_map = _merge_stats_maps(normal_map, gen_map)

        out.write(f"\n{'='*70}\n")
        out.write(f"Game: {gt.title} ({gt.id})\n")
        out.write(f"Directory: {game_dir}\n")

        for entry_type in ["Main Entry", "Extra Entry"]:
            has_data = any(
                stats_map.get((entry_type, cat), Stats()).file_count > 0
                for cat, _ in FILE_CATEGORIES
            )
            if not has_data:
                continue
            out.write(f"\n  {entry_type}:\n")
            for cat, _ in FILE_CATEGORIES:
                print_stats(out, cat, stats_map.get((entry_type, cat), Stats()))

        out.write(f"\n  Combined (all entries):\n")
        for cat, _ in FILE_CATEGORIES:
            print_combined_stats(out, cat, stats_map)

        if _has_any_files(gen_map):
            out.write(f"\n  Generated files (auto-detected):\n")
            for cat, _ in FILE_CATEGORIES:
                print_combined_stats(out, cat, gen_map)
            out.write(f"\n  Non-generated files:\n")
            for cat, _ in FILE_CATEGORIES:
                print_combined_stats(out, cat, normal_map)

    out.write(f"\n{'='*70}\n")
    out.write(f"Detected {len(game_data)} / {len(GAME_TYPES)} game(s).\n")


def report_summary(out: TextIO, game_data: list) -> None:
    """Condensed per-game table: file/line totals and generated percentages."""
    out.write("=== Game File Statistics Summary ===\n\n")
    if not game_data:
        out.write("  No locally installed Paradox games detected.\n")
        return

    w = 26
    hdr = f"  {'Game':<{w}} {'Files':>8}  {'Lines':>12}  {'GenFiles':>9}  {'GenLines':>12}  {'Gen%':>6}\n"
    sep = f"  {'-'*w} {'-'*8}  {'-'*12}  {'-'*9}  {'-'*12}  {'-'*6}\n"
    out.write(hdr)
    out.write(sep)

    grand_files = grand_lines = grand_gen_files = grand_gen_lines = 0
    for gt, game_dir, normal_map, gen_map in game_data:
        stats_map = _merge_stats_maps(normal_map, gen_map)
        all_files = _total_files(stats_map)
        all_lines = _total_lines(stats_map)
        gen_files = _total_files(gen_map)
        gen_lines = _total_lines(gen_map)
        gen_pct   = gen_lines / all_lines * 100 if all_lines else 0.0
        grand_files += all_files; grand_lines += all_lines
        grand_gen_files += gen_files; grand_gen_lines += gen_lines
        name = gt.title[:w]
        out.write(f"  {name:<{w}} {all_files:>8,}  {all_lines:>12,}  {gen_files:>9,}  {gen_lines:>12,}  {gen_pct:>5.1f}%\n")

    out.write(sep)
    grand_pct = grand_gen_lines / grand_lines * 100 if grand_lines else 0.0
    out.write(f"  {'TOTAL':<{w}} {grand_files:>8,}  {grand_lines:>12,}  {grand_gen_files:>9,}  {grand_gen_lines:>12,}  {grand_pct:>5.1f}%\n")
    out.write(f"\n  Detected {len(game_data)} / {len(GAME_TYPES)} game(s).\n")


def main() -> None:
    parser = argparse.ArgumentParser(description="Game file statistics audit")
    parser.add_argument("--output", "-o", metavar="FILE",
                        help="Write report to FILE instead of stdout")
    parser.add_argument("--summary", "-s", action="store_true",
                        help="Print condensed summary table instead of full report")
    args = parser.parse_args()

    # Collect per-game data
    game_data = []
    for gt in GAME_TYPES:
        game_dir = get_steam_game_path(gt.steam_id, gt.title)
        if game_dir is None or not os.path.isdir(game_dir):
            continue
        normal_map, gen_map = collect_game_stats(game_dir, gt.entry_info)
        game_data.append((gt, game_dir, normal_map, gen_map))

    # Open output destination
    if args.output:
        out = open(args.output, "w", encoding="utf-8")
    else:
        out = sys.stdout

    try:
        if args.summary:
            report_summary(out, game_data)
        else:
            report_detailed(out, game_data)
    finally:
        if args.output:
            out.close()
            print(f"Report written to: {args.output}", file=sys.stderr)

    print(f"Detected {len(game_data)} game(s).", file=sys.stderr)


if __name__ == "__main__":
    main()
