#!/usr/bin/env python3
import os
import re
import sys
from urllib.parse import unquote

def validate_markdown_links(scan_dirs=None, scan_files=None):
    if scan_dirs is None:
        scan_dirs = ['documents', 'docs']
    if scan_files is None:
        scan_files = ['AGENTS.md', 'CONTRIBUTING.md', 'README.md', 'README_zh.md']

    all_files = []
    for f in scan_files:
        if os.path.isfile(f):
            all_files.append(f)

    for d in scan_dirs:
        if not os.path.isdir(d):
            continue
        for root, dirs, files in os.walk(d):
            # Exclude node_modules and hidden/dot directories
            dirs[:] = [sub for sub in dirs if sub != 'node_modules' and not sub.startswith('.')]
            for f in files:
                if f.endswith('.md'):
                    all_files.append(os.path.join(root, f))

    md_link_regex = re.compile(r'\[([^\]]+)\]\(([^)]+)\)')
    broken_links = []
    total_links = 0

    for fpath in all_files:
        with open(fpath, 'r', encoding='utf-8') as file:
            text = file.read()

        clean_text = re.sub(r'```.*?```', '', text, flags=re.DOTALL)
        clean_text = re.sub(r'`.*?`', '', clean_text)

        for m in md_link_regex.finditer(clean_text):
            target = m.group(2).strip()

            if (target.startswith('http://') or target.startswith('https://') or 
                target.startswith('#') or target.startswith('mailto:')):
                continue
            if '$' in target:
                continue

            total_links += 1

            target_clean = target.split('#')[0]
            if not target_clean:
                continue

            target_decoded = unquote(target_clean)
            current_dir = os.path.dirname(fpath)

            if target_decoded.startswith('/'):
                resolved_path = os.path.normpath(os.path.join('.', target_decoded.lstrip('/')))
            else:
                resolved_path = os.path.normpath(os.path.join(current_dir, target_decoded))

            if not os.path.exists(resolved_path):
                broken_links.append((fpath, target, resolved_path))

    print(f"Scanned {total_links} internal Markdown links across {len(all_files)} files.")
    if broken_links:
        print(f"Found {len(broken_links)} broken links:")
        for src, tgt, resolved in broken_links:
            print(f"  In {src}: target '{tgt}' -> '{resolved}' NOT FOUND")
        return False
    else:
        print("All internal Markdown links are valid! 0 broken links found.")
        return True

if __name__ == '__main__':
    success = validate_markdown_links()
    sys.exit(0 if success else 1)
