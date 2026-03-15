# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

# mod_insights.py — Mod development environment detector + Steam Workshop stats fetcher.
#
# For each locally installed Paradox game, scans Workshop-downloaded mods and:
#   1. Detects the development environment (IDEA / VSCode / Unknown) via filesystem heuristics.
#   2. Fetches Steam Workshop statistics (views, subscriptions, favorites) via the public API.
#
# Dev environment heuristics (non-exclusive — a mod may match both):
#   IDEA (JetBrains):               .idea/  |  *.iml (mod root)  |  .config/ with *.cwt files
#   VSCode / VSCode-based AI IDEs:  .vscode/  |  _cwtools/  |  .cwtools  |  .cursor/  |  .windsurf/
#
# NOTE: Many mod authors exclude IDE config dirs when uploading to Workshop.
#       An "Unknown" result does NOT mean no IDE was used.
#
# Steam Workshop API used (no API key required):
#   POST https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/
#   Returns: views, subscriptions (current), favorited (current)
#   Note: "views" from the API may differ slightly from the "Unique Visitors" counter
#   shown on the Workshop page.
#
# Workshop content path is derived from the game's install directory, so secondary
# Steam library folders are handled correctly.
#
# Output modes:
#   default    : full console report
#   --markdown : write full Markdown report (to --output FILE, or a timestamped file)
#
# Usage:
#   python scripts/mod_insights.py [--markdown] [--output FILE]

from __future__ import annotations

import argparse
import json
import os
import platform
import re
import sys
import urllib.parse
import urllib.request
from dataclasses import dataclass, field
from datetime import datetime
from typing import TextIO

# ===========================================================================
# Game metadata (mirrored from ParadoxGameType + game_file_stats.py)
# ===========================================================================

@dataclass
class EntryInfo:
    game_main: list[str] = field(default_factory=list)
    game_extra: list[str] = field(default_factory=list)


@dataclass
class GameType:
    id: str
    title: str
    game_id: str
    steam_id: str
    entry_info: EntryInfo


_COMMON_EXTRA = ["clausewitz", "jomini"]

GAME_TYPES: list[GameType] = [
    GameType("stellaris", "Stellaris",            "stellaris",      "281990",
             EntryInfo(game_extra=["pdx_launcher/game", "pdx_launcher/common",
                                   "pdx_online_assets", "previewer_assets", "tweakergui_assets"])),
    GameType("ck2",  "Crusader Kings II",          "ck2",            "203770",  EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("ck3",  "Crusader Kings III",         "ck3",            "1158310", EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("eu4",  "Europa Universalis IV",      "eu4",            "236850",  EntryInfo(game_extra=_COMMON_EXTRA)),
    GameType("eu5",  "Europa Universalis V",       "eu5",            "3450310",
             EntryInfo(
                 game_main=["game/in_game", "game/main_menu", "game/loading_screen",
                            "game/dlc/*/in_game", "game/dlc/*/main_menu", "game/dlc/*/loading_screen"],
                 game_extra=["clausewitz/main_menu", "clausewitz/loading_screen",
                             "jomini/main_menu", "jomini/loading_screen"],
             )),
    GameType("hoi4", "Hearts of Iron IV",          "hoi4",           "394360",  EntryInfo(game_extra=_COMMON_EXTRA)),
    GameType("ir",   "Imperator Rome",             "imperator_rome", "859580",  EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("vic2", "Victoria 2",                 "victoria2",      "42960",   EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
    GameType("vic3", "Victoria 3",                 "victoria3",      "529340",  EntryInfo(game_main=["game"], game_extra=_COMMON_EXTRA)),
]

# ===========================================================================
# Steam path detection (mirrored from PlsPathServiceImpl + game_file_stats.py)
# ===========================================================================

_steam_path_cache: dict[str, str | None] = {}


def get_steam_path() -> str | None:
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


def get_workshop_content_path(steam_id: str) -> str | None:
    """Get the Workshop content directory for a game by scanning all Steam library folders."""
    steam = get_steam_path()
    if not steam:
        return None
    for library in _get_library_folders(steam):
        p = os.path.join(library, "steamapps", "workshop", "content", steam_id)
        if os.path.isdir(p):
            return p
    return None

# ===========================================================================
# Dev environment detection
# ===========================================================================

# Directories that indicate a JetBrains IDE (IDEA, etc.)
_IDEA_DIRS  = [".idea"]
# Directories/files that indicate VSCode or a VSCode-based AI IDE (Cursor, Windsurf, etc.)
_VSCODE_DIRS = [".vscode", "_cwtools", ".cursor", ".windsurf"]
_VSCODE_MISC = [".cwtools"]   # may be a file or directory

# Patterns looked up in .gitignore as fallback indicators
# (stripped of leading/trailing slashes before matching)
_GITIGNORE_IDEA_PATTERNS  = {".idea", "*.iml", ".config"}
_GITIGNORE_VSCODE_PATTERNS = {".vscode", "_cwtools", ".cwtools", ".cursor", ".windsurf"}


def _has_cwt_config_dir(mod_dir: str) -> bool:
    """Return True if .config/ exists and contains at least one .cwt file (PLS custom config dir)."""
    config_dir = os.path.join(mod_dir, ".config")
    if not os.path.isdir(config_dir):
        return False
    try:
        return any(fn.endswith(".cwt") for fn in os.listdir(config_dir))
    except OSError:
        return False


def parse_gitignore_ide_hints(mod_dir: str) -> tuple[list[str], list[str]]:
    """Parse .gitignore and return (idea_hints, vsc_hints) based on known IDE patterns.

    Used as a fallback when direct filesystem indicators are absent — mod authors
    often exclude IDE config directories before uploading, but leave .gitignore intact.
    Returned pattern strings are the raw (normalized) patterns found in the file.
    """
    gitignore_path = os.path.join(mod_dir, ".gitignore")
    if not os.path.isfile(gitignore_path):
        return [], []
    idea_hints: list[str] = []
    vsc_hints:  list[str] = []
    seen: set[str] = set()
    try:
        with open(gitignore_path, encoding="utf-8-sig", errors="ignore") as f:
            for line in f:
                pattern = line.strip().lstrip("/").rstrip("/")
                if not pattern or pattern.startswith("#") or pattern in seen:
                    continue
                seen.add(pattern)
                if pattern in _GITIGNORE_IDEA_PATTERNS:
                    idea_hints.append(pattern)
                elif pattern in _GITIGNORE_VSCODE_PATTERNS:
                    vsc_hints.append(pattern)
    except OSError:
        pass
    return idea_hints, vsc_hints


def detect_ide(mod_dir: str) -> tuple[list[str], list[str]]:
    """Detect IDE usage indicators in mod_dir.

    Returns (ides, indicators):
      ides       — IDE labels found, e.g. ["IDEA"], ["VSCode"], ["IDEA", "VSCode"], or []
      indicators — filesystem signals; direct ones are plain (e.g. ".idea"),
                   gitignore-inferred ones are prefixed with "~" (e.g. "~.idea")
    """
    idea_direct: list[str] = []
    vsc_direct:  list[str] = []

    # --- Direct filesystem detection ---
    if os.path.isdir(os.path.join(mod_dir, ".idea")):
        idea_direct.append(".idea")
    try:
        for fn in os.listdir(mod_dir):
            if fn.endswith(".iml"):
                idea_direct.append(".iml")
                break
    except OSError:
        pass
    if _has_cwt_config_dir(mod_dir):
        idea_direct.append(".config")
    for dirname in _VSCODE_DIRS:
        if os.path.isdir(os.path.join(mod_dir, dirname)):
            vsc_direct.append(dirname)
    for name in _VSCODE_MISC:
        if os.path.exists(os.path.join(mod_dir, name)):
            vsc_direct.append(name)

    # --- .gitignore fallback (only add hints not already covered directly) ---
    gi_idea, gi_vsc = parse_gitignore_ide_hints(mod_dir)
    idea_direct_set = set(idea_direct)
    vsc_direct_set  = set(vsc_direct)
    idea_gi = [f"~{h}" for h in gi_idea if h not in idea_direct_set]
    vsc_gi  = [f"~{h}" for h in gi_vsc  if h not in vsc_direct_set]

    all_idea = idea_direct + idea_gi
    all_vsc  = vsc_direct  + vsc_gi

    ides: list[str] = []
    if all_idea:
        ides.append("IDEA")
    if all_vsc:
        ides.append("VSCode")
    return ides, all_idea + all_vsc

# ===========================================================================
# Mod descriptor
# ===========================================================================

def read_mod_name(mod_dir: str) -> str | None:
    """Read the mod name from descriptor.mod (or any *.mod file) at mod root."""
    candidates: list[str] = []
    desc = os.path.join(mod_dir, "descriptor.mod")
    if os.path.isfile(desc):
        candidates.append(desc)
    try:
        for fn in os.listdir(mod_dir):
            if fn.endswith(".mod") and fn != "descriptor.mod":
                candidates.append(os.path.join(mod_dir, fn))
    except OSError:
        pass
    for path in candidates:
        try:
            with open(path, encoding="utf-8-sig", errors="ignore") as f:
                for line in f:
                    s = line.strip()
                    if s.startswith("name"):
                        parts = s.split("=", 1)
                        if len(parts) == 2:
                            return parts[1].strip().strip('"')
        except OSError:
            pass
    return None

# ===========================================================================
# Data model
# ===========================================================================

@dataclass
class ModInfo:
    item_id:        str
    game_id:        str
    game_title:     str
    mod_dir:        str
    mod_name:       str | None
    ides:           list[str]   # e.g. ["IDEA"], ["VSCode"], ["IDEA", "VSCode"], []
    ide_indicators: list[str]   # specific filesystem signals
    views:          int | None = None
    subscriptions:  int | None = None
    favorited:      int | None = None

    @property
    def ide_label(self) -> str:
        return " + ".join(self.ides) if self.ides else "Unknown"

    @property
    def indicators_str(self) -> str:
        return ", ".join(self.ide_indicators) if self.ide_indicators else "—"

# ===========================================================================
# Mod discovery
# ===========================================================================

def discover_mods(gt: GameType, ws_path: str) -> list[ModInfo]:
    """Discover all Workshop-installed mods under ws_path."""
    mods: list[ModInfo] = []
    try:
        entries = sorted(os.scandir(ws_path), key=lambda e: e.name)
    except OSError:
        return mods
    for entry in entries:
        if not entry.is_dir():
            continue
        mod_dir  = entry.path
        mod_name = read_mod_name(mod_dir)
        ides, indicators = detect_ide(mod_dir)
        mods.append(ModInfo(
            item_id=entry.name,
            game_id=gt.id,
            game_title=gt.title,
            mod_dir=mod_dir,
            mod_name=mod_name,
            ides=ides,
            ide_indicators=indicators,
        ))
    return mods

# ===========================================================================
# Steam Workshop API
# ===========================================================================

_API_URL    = "https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/"
_CHUNK_SIZE = 100


def fetch_workshop_stats(item_ids: list[str]) -> dict[str, dict]:
    """Batch-fetch Workshop stats; returns mapping item_id → detail dict."""
    result: dict[str, dict] = {}
    for i in range(0, len(item_ids), _CHUNK_SIZE):
        chunk = item_ids[i:i + _CHUNK_SIZE]
        params: dict[str, str] = {"itemcount": str(len(chunk))}
        for j, fid in enumerate(chunk):
            params[f"publishedfileids[{j}]"] = fid
        data = urllib.parse.urlencode(params).encode()
        try:
            req = urllib.request.Request(
                _API_URL, data=data,
                headers={"Content-Type": "application/x-www-form-urlencoded"},
            )
            with urllib.request.urlopen(req, timeout=20) as resp:
                payload = json.loads(resp.read().decode())
            for detail in payload.get("response", {}).get("publishedfiledetails", []):
                fid = str(detail.get("publishedfileid", ""))
                if fid:
                    result[fid] = detail
        except Exception as exc:
            print(f"  [warn] Steam API error (chunk {i // _CHUNK_SIZE + 1}): {exc}", file=sys.stderr)
    return result


def apply_stats(mods: list[ModInfo], stats: dict[str, dict]) -> None:
    for mod in mods:
        detail = stats.get(mod.item_id, {})
        mod.views         = detail.get("views")
        mod.subscriptions = detail.get("subscriptions")
        mod.favorited     = detail.get("favorited")

# ===========================================================================
# Formatting helpers
# ===========================================================================

def _fmt(v: int | None, width: int = 0) -> str:
    if v is None:
        s = "—"
    else:
        s = f"{v:,}"
    return s.rjust(width) if width else s


def _ide_counts(mods: list[ModInfo]) -> tuple[int, int, int, int]:
    """Return (idea_only, vsc_only, both, unknown) counts."""
    idea  = sum(1 for m in mods if "IDEA" in m.ides and "VSCode" not in m.ides)
    vsc   = sum(1 for m in mods if "VSCode" in m.ides and "IDEA" not in m.ides)
    both  = sum(1 for m in mods if "IDEA" in m.ides and "VSCode" in m.ides)
    unk   = sum(1 for m in mods if not m.ides)
    return idea, vsc, both, unk


def _ide_summary(mods: list[ModInfo]) -> str:
    idea, vsc, both, unk = _ide_counts(mods)
    parts = [f"IDEA: {idea}", f"VSCode: {vsc}"]
    if both:
        parts.append(f"Both: {both}")
    parts.append(f"Unknown: {unk}")
    return " | ".join(parts)


def _md_table(headers: list, rows: list, col_aligns: list[str] | None = None) -> str:
    aligns = col_aligns or ["---"] * len(headers)
    lines = [
        "| " + " | ".join(str(h) for h in headers) + " |",
        "| " + " | ".join(aligns) + " |",
    ]
    for row in rows:
        lines.append("| " + " | ".join(str(c) for c in row) + " |")
    return "\n".join(lines)

# ===========================================================================
# Console report
# ===========================================================================

# (gt, ws_path, mods)
GameEntry = tuple[GameType, str, list[ModInfo]]


def report_console(out: TextIO, game_entries: list[GameEntry]) -> None:
    out.write("=== Mod Development Insights ===\n")

    all_mods = [m for _, _, mods in game_entries for m in mods]
    if not all_mods:
        out.write("\nNo Workshop-installed mods found.\n")
        return

    id_w   = 18
    name_w = 32
    ide_w  = 12
    ind_w  = 26
    num_w  = 10

    for gt, ws_path, mods in game_entries:
        out.write(f"\n{'─' * 80}\n")
        out.write(f"  {gt.title}  [{gt.steam_id}]\n")
        out.write(f"  Workshop: {ws_path}\n")
        out.write(f"  Mods: {len(mods)} | {_ide_summary(mods)}\n\n")

        hdr = (f"  {'Item ID':<{id_w}} {'Mod Name':<{name_w}} {'IDE':<{ide_w}}"
               f" {'Indicators':<{ind_w}} {'Views':>{num_w}} {'Subs':>{num_w}} {'Favs':>{num_w}}\n")
        sep = (f"  {'-'*id_w} {'-'*name_w} {'-'*ide_w}"
               f" {'-'*ind_w} {'-'*num_w} {'-'*num_w} {'-'*num_w}\n")
        out.write(hdr)
        out.write(sep)
        for m in mods:
            name = (m.mod_name or "")[:name_w]
            ide  = m.ide_label[:ide_w]
            ind  = m.indicators_str[:ind_w]
            out.write(
                f"  {m.item_id:<{id_w}} {name:<{name_w}} {ide:<{ide_w}}"
                f" {ind:<{ind_w}} {_fmt(m.views, num_w)}"
                f" {_fmt(m.subscriptions, num_w)} {_fmt(m.favorited, num_w)}\n"
            )

    out.write(f"\n{'─' * 80}\n")
    games_with_mods = len(game_entries)
    out.write(f"  Total: {len(all_mods)} mods across {games_with_mods} game(s)\n")
    out.write(f"  {_ide_summary(all_mods)}\n")

# ===========================================================================
# Markdown report
# ===========================================================================

def report_markdown(out: TextIO, game_entries: list[GameEntry]) -> None:
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    out.write("# Mod Development Insights\n\n")
    out.write(f"> Generated: {ts}\n\n")
    out.write("> **Note on detection:** Heuristics rely on filesystem artifacts "
              "(.idea, .vscode, etc.). Many authors exclude these when uploading — "
              "an *Unknown* result does not mean no IDE was used.\n\n")
    out.write("> **Note on views:** The `views` field from the Steam API may differ "
              "slightly from the *Unique Visitors* counter shown on the Workshop page.\n\n")

    all_mods = [m for _, _, mods in game_entries for m in mods]
    if not all_mods:
        out.write("*No Workshop-installed mods found.*\n")
        return

    # Global summary
    out.write("## Summary\n\n")
    s_hdrs = ["Game", "Mods", "IDEA", "VSCode", "Both", "Unknown"]
    s_aln  = [":---", "---:", "---:", "---:", "---:", "---:"]
    s_rows = []
    for gt, _, mods in game_entries:
        idea, vsc, both, unk = _ide_counts(mods)
        s_rows.append([gt.title, len(mods), idea, vsc, both, unk])
    out.write(_md_table(s_hdrs, s_rows, s_aln) + "\n\n")

    # Per-game detail
    hdrs = ["Item ID", "Mod Name", "IDE", "Indicators", "Views", "Subscriptions", "Favorites"]
    aln  = [":---", ":---", ":---", ":---", "---:", "---:", "---:"]
    for gt, ws_path, mods in game_entries:
        out.write(f"## {gt.title}\n\n")
        out.write(f"Workshop: `{ws_path}`  \n")
        out.write(f"Mods: **{len(mods)}** | {_ide_summary(mods)}\n\n")
        rows = [
            [m.item_id, m.mod_name or "—", m.ide_label, m.indicators_str,
             _fmt(m.views), _fmt(m.subscriptions), _fmt(m.favorited)]
            for m in mods
        ]
        out.write(_md_table(hdrs, rows, aln) + "\n\n")

# ===========================================================================
# Entry point
# ===========================================================================

def main() -> None:
    parser = argparse.ArgumentParser(description="Mod dev environment detector + Workshop stats")
    parser.add_argument("--markdown", "--md", dest="markdown", action="store_true",
                        help="Write a full Markdown report document")
    parser.add_argument("--output", "-o", metavar="FILE",
                        help="Write output to FILE (for --markdown, defaults to a timestamped file)")
    args = parser.parse_args()

    # Discover mods for each installed game
    game_entries: list[GameEntry] = []
    for gt in GAME_TYPES:
        game_dir = get_steam_game_path(gt.steam_id, gt.title)
        if not game_dir:
            continue
        ws_path = get_workshop_content_path(gt.steam_id)
        if not ws_path:
            continue
        mods = discover_mods(gt, ws_path)
        if not mods:
            continue
        game_entries.append((gt, ws_path, mods))

    # Fetch Workshop stats
    all_ids = [m.item_id for _, _, mods in game_entries for m in mods]
    if all_ids:
        print(f"Fetching Workshop stats for {len(all_ids)} mod(s)…", file=sys.stderr)
        stats = fetch_workshop_stats(all_ids)
        for _, _, mods in game_entries:
            apply_stats(mods, stats)

    if args.markdown:
        if args.output:
            md_path = args.output
        else:
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            repo_root  = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
            report_dir = os.path.join(repo_root, "tmp", "reports")
            os.makedirs(report_dir, exist_ok=True)
            md_path = os.path.join(report_dir, f"mod_insights_{ts}.md")
        with open(md_path, "w", encoding="utf-8") as f:
            report_markdown(f, game_entries)
        print(f"Markdown report written to: {md_path}", file=sys.stderr)
    else:
        out = open(args.output, "w", encoding="utf-8") if args.output else sys.stdout
        try:
            report_console(out, game_entries)
        finally:
            if args.output:
                out.close()
                print(f"Report written to: {args.output}", file=sys.stderr)

    total = sum(len(mods) for _, _, mods in game_entries)
    print(f"Done: {total} mod(s) across {len(game_entries)} game(s).", file=sys.stderr)


if __name__ == "__main__":
    main()
