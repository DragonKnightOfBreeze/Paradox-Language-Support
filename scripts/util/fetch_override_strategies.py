# Copyright (c) 2026 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

"""fetch_override_strategies.py — Derive candidate Stellaris override strategies from IronyModManager (IMM).

The plugin's override strategies (``fios`` / ``lios`` / ``dupl`` / ``ordered``) describe how the
game loads and merges files under a given entry-relative directory. The game engine behaviour is
not publicly specified, so we approximate it from trusted/reference sources:

- **IMM (IronyModManager)** — a widely used, actively maintained mod manager. Its per-directory
  parser map (``StellarisParserMap.json``) and its list of "first in, only served" directories
  (``StellarisDefinitionInfoProvider.FIOSPaths``) encode most of the same knowledge that was
  previously curated by hand.
- **Stellaris Mod Group / English wiki overwrite list** — a human-maintained, per-directory list
  (see ``https://main--pdxdoc-next.netlify.app/guides/other/overwrite/``), used to validate and
  to resolve the cases where IMM applies whole-text handling only as an implementation shortcut
  (for example ``common/on_actions``).

The script fetches the IMM sources at a pinned commit (resolved from a branch/tag) for
reproducibility, derives a candidate strategy per directory, and diffs it against an existing
``overrides.cwt`` file so the maintainer can review the discrepancies manually.

Inference rules (conservative, IMM-oriented):

- directory matches an IMM ``FIOSPaths`` entry -> ``fios``
- parser ends with ``WholeText`` -> ``dupl`` (the whole file is taken as a unit)
- parser is ``StellarisOverwrittenObjectSingleFileParser`` -> ``dupl`` (whole-file replacement)
- parser contains ``OverwrittenParser`` -> ``lios`` (key-based object replacement)
- otherwise -> ``lios`` (default key override)

Known special cases are listed in ``SPECIAL_CASES`` (e.g. ``common/on_actions = ordered``, which
the engine merges but IMM treats as whole text).

Usage:
  python scripts/util/fetch_override_strategies.py
  python scripts/util/fetch_override_strategies.py --ref master --out build/overrides.md
  python scripts/util/fetch_override_strategies.py --config cwt/cwtools-stellaris-config/config/overrides.cwt
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import urllib.error
import urllib.request
from datetime import date
from pathlib import Path

IMM_OWNER = "bcssov"
IMM_REPO = "IronyModManager"
IMM_PROVIDER_PATH = "src/IronyModManager.IO/Mods/InfoProviders/StellarisDefinitionInfoProvider.cs"
IMM_PARSER_MAP_PATH = "References/CopyAll/Maps/StellarisParserMap.json"

GITHUB_API = "https://api.github.com"
GITHUB_RAW = "https://raw.githubusercontent.com"

# Entry-relative directory paths that IMM treats as "first in, only served".
# NOTE: This is a fallback/documentation copy; the authoritative list is parsed from the
# IMM provider source at runtime by `parse_fios_paths`.
FIOS_PATHS_FALLBACK = {
    "component_sets",
    "component_templates",
    "event_chains",
    "global_ship_designs",
    "scripted_variables",
    "section_templates",
    "ship_behaviors",
    "special_projects",
    "strategic_resources",
    "events",
    "solar_system_initializers",
    "traits",
    "start_screen_messages",
    "governments/authorities",
}

# Engine behaviours we do not want to take from IMM verbatim.
SPECIAL_CASES = {
    # The engine merges on-actions by key, but IMM only knows how to replace the whole file.
    "common/on_actions": ("ordered", "Stellaris wiki / pdxdoc overwrite guide (auto-merge); IMM uses whole-text as a shortcut"),
}


def http_get(url: str, *, accept: str | None = None) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": "paradox-chronicle-research"})
    if accept:
        request.add_header("Accept", accept)
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read()


def resolve_commit(ref: str) -> str:
    url = f"{GITHUB_API}/repos/{IMM_OWNER}/{IMM_REPO}/commits/{ref}"
    data = json.loads(http_get(url, accept="application/vnd.github+json"))
    return data["sha"]


def fetch_text(ref: str, path: str) -> str:
    url = f"{GITHUB_RAW}/{IMM_OWNER}/{IMM_REPO}/{ref}/{path}"
    return http_get(url).decode("utf-8", errors="replace")


def parse_fios_paths(provider_source: str) -> set[str]:
    """Extract the IMM ``FIOSPaths`` entries from the provider C# source.

    IMM matches these with ``ParentDirectory.EndsWith(path)``; entries may be multi-segment
    (for example ``governments\\authorities``). Returned as normalised ``/``-separated paths.
    """
    block_match = re.search(r"FIOSPaths\s*=>\s*\[(.*?)]", provider_source, re.DOTALL)
    if not block_match:
        raise ValueError("Could not locate FIOSPaths in the IMM provider source.")
    paths = re.findall(r'"([^"]+)"', block_match.group(1))
    return {path.replace("\\", "/") for path in paths}


# Nested inline scripts are directories whose leaf names collide with top-level FIOS keywords
# (for example ``common/inline_scripts/events``). They are not top-level FIOS directories.
INLINE_SCRIPTS_PREFIX = "common/inline_scripts"


def infer_strategy(directory_path: str, parser: str, fios_paths: set[str]) -> str:
    key = directory_path.replace("\\", "/")
    if not key.startswith(INLINE_SCRIPTS_PREFIX):
        if any(key == path or key.endswith("/" + path) for path in fios_paths):
            return "fios"
    if parser.endswith("WholeTextParser"):
        return "dupl"
    if parser == "StellarisOverwrittenObjectSingleFileParser":
        return "dupl"
    if parser.endswith("OverwrittenParser"):
        return "lios"
    return "lios"


def derive_table(parser_map: list[dict], fios_paths: set[str]) -> dict[str, tuple[str, str]]:
    """Return ``entry-relative path -> (strategy, evidence)`` for relevant directories."""
    result: dict[str, tuple[str, str]] = {}
    for item in parser_map:
        directory = item.get("DirectoryPath", "").replace("\\", "/")
        parser = item.get("PreferredParser", "")
        if directory == "common" or not (directory == "events" or directory.startswith("common/") or directory == "prescripted_countries"):
            continue
        result[directory] = (infer_strategy(directory, parser, fios_paths), f"IMM parser: {parser}")
    for path, (strategy, evidence) in SPECIAL_CASES.items():
        result[path] = (strategy, evidence)
    return result


def parse_existing_config(path: Path) -> dict[str, str]:
    """Parse a minimal ``overrides = { "path" = strategy }`` cwt block."""
    if not path.is_file():
        return {}
    content = path.read_text(encoding="utf-8", errors="replace")
    return {match.group(1): match.group(2).lower() for match in re.finditer(r'"([^"]+)"\s*=\s*(\w+)', content)}


def render_markdown(ref: str, commit: str, table: dict[str, tuple[str, str]], existing: dict[str, str]) -> str:
    lines: list[str] = []
    lines.append(f"# Stellaris Override Strategy Candidates (IMM {ref} @ {commit[:10]})")
    lines.append("")
    lines.append(f"- Source: `{GITHUB_RAW}/{IMM_OWNER}/{IMM_REPO}/{commit}`")
    lines.append(f"- Generated: {date.today().isoformat()}")
    lines.append("- Method: `scripts/util/fetch_override_strategies.py`")
    lines.append("")
    lines.append("| Entry path | Candidate | Current | Evidence |")
    lines.append("| --- | --- | --- | --- |")
    for path in sorted(table):
        strategy, evidence = table[path]
        current = existing.get(path, "-")
        mark = "" if current == strategy else " **(!)**"
        lines.append(f"| `{path}` | `{strategy}` | `{current}`{mark} | {evidence} |")
    lines.append("")
    lines.append("## Differences against the current config")
    lines.append("")
    differences = [
        (path, *table[path], existing.get(path))
        for path in sorted(table)
        if existing.get(path) not in (None, table[path][0])
    ]
    if not differences:
        lines.append("- None.")
    else:
        for path, strategy, evidence, current in differences:
            lines.append(f"- `{path}`: `{current}` -> `{strategy}` ({evidence})")
    lines.append("")
    return "\n".join(lines)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Derive Stellaris override strategies from IronyModManager.")
    parser.add_argument("--ref", default="master", help="IMM branch/tag/commit to inspect (default: master).")
    parser.add_argument("--out", type=Path, help="Write the Markdown report to this file instead of stdout.")
    parser.add_argument(
        "--config",
        type=Path,
        default=Path("cwt/cwtools-stellaris-config/config/overrides.cwt"),
        help="Existing overrides.cwt used for the difference report.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        commit = resolve_commit(args.ref)
        provider_source = fetch_text(commit, IMM_PROVIDER_PATH)
        parser_map = json.loads(fetch_text(commit, IMM_PARSER_MAP_PATH))
    except (urllib.error.URLError, KeyError, ValueError) as error:
        print(f"ERROR: failed to fetch IMM sources: {error}", file=sys.stderr)
        return 1

    fios_paths = parse_fios_paths(provider_source) or FIOS_PATHS_FALLBACK

    table = derive_table(parser_map, fios_paths)
    existing = parse_existing_config(args.config)
    report = render_markdown(args.ref, commit, table, existing)

    if args.out:
        args.out.parent.mkdir(parents=True, exist_ok=True)
        args.out.write_text(report, encoding="utf-8", newline="\n")
        print(f"Wrote report: {args.out}")
    else:
        print(report)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
