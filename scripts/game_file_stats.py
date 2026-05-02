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
#     ParadoxGameType, ParadoxGameTypeMetadata, and PlsPathServiceImpl.
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
#   default   : full detailed report (per-entry-type breakdown + combined + generated)
#   --summary : condensed per-game table with file/line totals and generated percentages
#   --markdown: full markdown document (saved to file)
#
# Output defaults to stdout; use --output FILE to write to a file.
# For --markdown, output defaults to a timestamped file under tmp/reports/.
#
# Usage:
#   python scripts/game_file_stats.py [--summary] [--markdown] [--output FILE]

from __future__ import annotations

import argparse
import glob
import os
import platform
import re
import sys
from datetime import datetime
from collections import defaultdict
from dataclasses import dataclass, field
from typing import Iterable, TextIO

# ===========================================================================
# Game metadata (synced from ParadoxGameType + ParadoxGameTypeMetadata)
# ===========================================================================

@dataclass
class GameType:
    """Mirrors ParadoxGameType (game entries only, no Core)."""
    id: str
    title: str
    game_id: str
    steam_id: str
    entry_info: GameTypeMetadata

@dataclass
class GameTypeMetadata:
    """Mirrors ParadoxGameTypeMetadata.  Only game-side entries are needed here."""
    game_main: list[str] = field(default_factory=list)
    game_extra: list[str] = field(default_factory=list)


# Entries object mirrors ParadoxGameType.Entries / GameTypeMetadatas
_COMMON_EXTRA = ["clausewitz", "jomini"]

GAME_TYPES: list[GameType] = [
    GameType("stellaris", "Stellaris", "stellaris", "281990",
             GameTypeMetadata(game_extra=[
                 "pdx_launcher/game", "pdx_launcher/common",
                 "pdx_online_assets", "previewer_assets", "tweakergui_assets",
             ])),
    GameType("ck2", "Crusader Kings II", "ck2", "203770",
             GameTypeMetadata(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("ck3", "Crusader Kings III", "ck3", "1158310",
             GameTypeMetadata(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("eu4", "Europa Universalis IV", "eu4", "236850",
             GameTypeMetadata(game_extra=_COMMON_EXTRA)),
    GameType("eu5", "Europa Universalis V", "eu5", "3450310",
             GameTypeMetadata(
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
             GameTypeMetadata(game_extra=_COMMON_EXTRA)),
    GameType("ir", "Imperator Rome", "imperator_rome", "859580",
             GameTypeMetadata(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("vic2", "Victoria 2", "victoria2", "42960",
             GameTypeMetadata(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("vic3", "Victoria 3", "victoria3", "529340",
             GameTypeMetadata(game_main=["game"], game_extra=_COMMON_EXTRA)),
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
        import winreg
        # 64位系统：WOW6432Node 重定向键；32位系统回退到非重定向键
        for reg_path in (
            r"SOFTWARE\WOW6432Node\Valve\Steam",
            r"SOFTWARE\Valve\Steam",
        ):
            try:
                key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, reg_path)
                val, _ = winreg.QueryValueEx(key, "InstallPath")
                winreg.CloseKey(key)
                if val and os.path.isdir(val):
                    return val
            except (FileNotFoundError, OSError):
                pass
    else:
        home = os.path.expanduser("~")
        candidates = [
            os.path.join(home, ".local", "share", "Steam"),
            os.path.join(home, ".steam", "debian-installation"),
            os.path.join(home, ".steam", "steam"),
            os.path.join(home, "snap", "steam", "common", ".local", "share", "Steam"),
            os.path.join(home, ".var", "app", "com.valvesoftware.Steam", ".local", "share", "Steam"),
        ]
        for p in candidates:
            if os.path.isdir(p):
                return p
    return None


def _get_library_folders(steam_path: str) -> list[str]:
    """Parse steamapps/libraryfolders.vdf and return all known Steam library paths (including steam_path itself)."""
    libraries = [steam_path]
    vdf_path = os.path.join(steam_path, "steamapps", "libraryfolders.vdf")
    try:
        with open(vdf_path, encoding="utf-8") as f:
            content = f.read()
        for m in re.finditer(r'"path"\s+"([^"]+)"', content):
            raw = m.group(1).replace("\\\\", "\\")
            if os.path.isdir(raw) and raw not in libraries:
                libraries.append(raw)
    except OSError:
        pass
    return libraries


def get_steam_game_path(steam_id: str, game_title: str) -> str | None:
    """Get a Steam game's installation directory."""
    if steam_id in _steam_path_cache:
        return _steam_path_cache[steam_id]
    result = _do_get_steam_game_path(steam_id, game_title)
    _steam_path_cache[steam_id] = result
    return result


def _do_get_steam_game_path(steam_id: str, game_title: str) -> str | None:
    # Primary: scan all library folders via libraryfolders.vdf
    steam = get_steam_path()
    if steam:
        for library in _get_library_folders(steam):
            p = os.path.join(library, "steamapps", "common", game_title)
            if os.path.isdir(p):
                return p
    # Windows fallback: registry (64-bit then 32-bit)
    if platform.system() == "Windows":
        import winreg
        for reg_path in (
            rf"SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\Steam App {steam_id}",
            rf"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Steam App {steam_id}",
        ):
            try:
                key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, reg_path)
                val, _ = winreg.QueryValueEx(key, "InstallLocation")
                winreg.CloseKey(key)
                if val and os.path.isdir(val):
                    return val
            except (FileNotFoundError, OSError):
                pass
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


def collect_game_stats(game_dir: str, entry_info: GameTypeMetadata
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


def report_markdown(out, game_data: list) -> None:
    """Write a full Markdown report document."""
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    out.write("# Game File Statistics Report\n\n")
    out.write(f"> Generated: {ts}\n\n")

    if not game_data:
        out.write("*No locally installed Paradox games detected.*\n")
        return

    # Summary table
    out.write("## Summary\n\n")
    s_hdrs = ["Game", "Files", "Lines", "Gen Files", "Gen Lines", "Gen%"]
    s_aln  = [":---", "---:", "---:", "---:", "---:", "---:"]
    s_rows = []
    for gt, game_dir, normal_map, gen_map in game_data:
        sm = _merge_stats_maps(normal_map, gen_map)
        af = _total_files(sm);  al = _total_lines(sm)
        gf = _total_files(gen_map); gl = _total_lines(gen_map)
        s_rows.append([gt.title, f"{af:,}", f"{al:,}",
                       f"{gf:,}", f"{gl:,}", f"{gl/al*100:.1f}%" if al else "0.0%"])
    out.write(_md_table(s_hdrs, s_rows, s_aln) + "\n\n")
    out.write(f"Detected {len(game_data)} / {len(GAME_TYPES)} game(s).\n\n")

    # Per-game sections
    cat_hdrs = ["Entry Type", "Category", "Files", "Total", "Code", "Comment", "Blank"]
    cat_aln  = [":---", ":---", "---:", "---:", "---:", "---:", "---:"]
    comb_hdrs = ["Category", "Files", "Total", "Code", "Comment", "Blank"]
    comb_aln  = [":---", "---:", "---:", "---:", "---:", "---:"]

    for gt, game_dir, normal_map, gen_map in game_data:
        sm = _merge_stats_maps(normal_map, gen_map)
        out.write(f"## {gt.title}\n\n")
        out.write(f"Directory: `{game_dir}`\n\n")

        # Breakdown by entry type + category
        rows = []
        for et in ["Main Entry", "Extra Entry"]:
            for cat, _ in FILE_CATEGORIES:
                s = sm.get((et, cat), Stats())
                if s.file_count == 0:
                    continue
                rows.append([et, cat, f"{s.file_count:,}", f"{s.total_lines:,}",
                             f"{s.code_lines:,}", f"{s.comment_lines:,}", f"{s.blank_lines:,}"])
        if rows:
            out.write("### By Entry Type\n\n")
            out.write(_md_table(cat_hdrs, rows, cat_aln) + "\n\n")

        # Combined per category (sum across entry types)
        comb_rows = []
        for cat, _ in FILE_CATEGORIES:
            c = Stats()
            for et in ["Main Entry", "Extra Entry"]:
                s = sm.get((et, cat), Stats())
                if s.file_count > 0:
                    c.file_count += s.file_count
                    c.total_lines += s.total_lines
                    c.blank_lines += s.blank_lines
                    c.comment_lines += s.comment_lines
            if c.file_count > 0:
                comb_rows.append([cat, f"{c.file_count:,}", f"{c.total_lines:,}",
                                  f"{c.code_lines:,}", f"{c.comment_lines:,}", f"{c.blank_lines:,}"])
        if comb_rows:
            out.write("### Combined (All Entries)\n\n")
            out.write(_md_table(comb_hdrs, comb_rows, comb_aln) + "\n\n")

        # Generated files
        if _has_any_files(gen_map):
            gf = _total_files(gen_map); gl = _total_lines(gen_map)
            af = _total_files(sm);       al = _total_lines(sm)
            out.write("### Generated Files (auto-detected)\n\n")
            out.write(f"**{gf:,}** / {af:,} files ({gf/af*100:.1f}%), "
                      f"**{gl:,}** / {al:,} lines ({gl/al*100:.1f}%)\n\n")
            gen_rows = []
            for et in ["Main Entry", "Extra Entry"]:
                for cat, _ in FILE_CATEGORIES:
                    s = gen_map.get((et, cat), Stats())
                    if s.file_count == 0:
                        continue
                    gen_rows.append([et, cat, f"{s.file_count:,}", f"{s.total_lines:,}",
                                    f"{s.code_lines:,}", f"{s.comment_lines:,}", f"{s.blank_lines:,}"])
            if gen_rows:
                out.write(_md_table(cat_hdrs, gen_rows, cat_aln) + "\n\n")

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
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--summary", "-s", action="store_true",
                      help="Print condensed summary table instead of full report")
    mode.add_argument("--markdown", "--md", dest="markdown", action="store_true",
                      help="Write a full Markdown report document")
    parser.add_argument("--output", "-o", metavar="FILE",
                        help="Write output to FILE (for --markdown, defaults to a timestamped file)")
    args = parser.parse_args()

    # Collect per-game data
    game_data = []
    for gt in GAME_TYPES:
        game_dir = get_steam_game_path(gt.steam_id, gt.title)
        if game_dir is None or not os.path.isdir(game_dir):
            continue
        normal_map, gen_map = collect_game_stats(game_dir, gt.entry_info)
        game_data.append((gt, game_dir, normal_map, gen_map))

    if args.markdown:
        if args.output:
            md_path = args.output
        else:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
            report_dir = os.path.join(repo_root, "tmp", "reports")
            os.makedirs(report_dir, exist_ok=True)
            md_path = os.path.join(report_dir, f"game_file_stats_{ts}.md")
        with open(md_path, "w", encoding="utf-8") as f:
            report_markdown(f, game_data)
        print(f"Markdown report written to: {md_path}", file=sys.stderr)
    else:
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
