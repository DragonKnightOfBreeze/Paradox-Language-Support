# Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
# All rights reserved.

"""run_reports.py — Facade runner: generate all six audit reports in Markdown format.

Runs each of the six audit scripts with --markdown, saving output to:
  documents/reports/{timestamp}/{script_name}.md

Usage:
  python scripts/run_reports.py
"""

from __future__ import annotations

import os
import subprocess
import sys
from datetime import datetime

# ---------------------------------------------------------------------------
# Script registry
# ---------------------------------------------------------------------------

# (script_filename, extra_cli_args)
SCRIPTS: list[tuple[str, list[str]]] = [
    ("code_stats.py",         []),
    ("code_hotspots.py",      []),
    ("config_stats.py",       []),
    ("config_hotspots.py",    []),
    ("game_file_stats.py",    []),
    ("game_file_hotspots.py", []),
]

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    repo_root   = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    scripts_dir = os.path.join(repo_root, "scripts")

    ts      = datetime.now().strftime("%Y%m%d_%H%M%S")
    out_dir = os.path.join(repo_root, "documents", "reports", ts)
    os.makedirs(out_dir, exist_ok=True)

    print(f"Output directory: {out_dir}")
    print()

    results: list[tuple[str, str, bool]] = []

    for script_name, extra_args in SCRIPTS:
        stem      = os.path.splitext(script_name)[0]
        md_path   = os.path.join(out_dir, f"{stem}.md")
        cmd       = [sys.executable, os.path.join(scripts_dir, script_name),
                     "--markdown", "--output", md_path] + extra_args

        print(f"  [{stem}]", flush=True)
        # Let stderr flow so scanning progress is visible in real time.
        result = subprocess.run(cmd, stdout=subprocess.PIPE, text=True)
        ok = result.returncode == 0
        results.append((script_name, md_path, ok))
        if not ok:
            print(f"    ERROR: exit code {result.returncode}")

    # Final summary
    print()
    n_ok   = sum(1 for _, _, ok in results if ok)
    n_fail = len(results) - n_ok
    print(f"Done: {n_ok}/{len(results)} scripts succeeded.")
    if n_fail:
        print("Failed:")
        for name, _, ok in results:
            if not ok:
                print(f"  - {name}")
    else:
        print(f"All reports saved to: {out_dir}")


if __name__ == "__main__":
    main()
