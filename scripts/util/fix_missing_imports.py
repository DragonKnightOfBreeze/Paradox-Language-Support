"""修复 Kotlin 文件中缺失的显式 import。

扫描 src/main/kotlin 和 src/test/kotlin 中的所有 .kt 文件，
如果某个预定义的标识符被使用但没有对应的显式 import / 通配符 import，
则在现有 import 语句末尾补上缺失的 import。
"""

import re
from pathlib import Path

# 需要显式导入的 FQName -> 简单标识符 映射
FQ_NAME_MAP: dict[str, str] = {
    "icu.windea.pls.model.CwtType": "CwtType",
    "icu.windea.pls.model.CwtSeparatorType": "CwtSeparatorType",
    "icu.windea.pls.model.ParadoxGameType": "ParadoxGameType",
    "icu.windea.pls.model.ParadoxDefinitionSource": "ParadoxDefinitionSource",
    "icu.windea.pls.model.ParadoxLocalisationType": "ParadoxLocalisationType",
}

SCAN_DIRS: list[str] = [
    "src/main/kotlin",
    "src/test/kotlin",
]


def _package_of(fq_name: str) -> str:
    return ".".join(fq_name.split(".")[:-1])


def _is_imported(import_stmts: list[str], fq_name: str, pkg: str) -> bool:
    for stmt in import_stmts:
        base = stmt.removeprefix("import ").strip()
        base = base.split(" as ")[0].strip()
        if base == fq_name:
            return True
        if base.endswith(".*") and base[:-2] == pkg:
            return True
    return False


def process_file(file_path: Path) -> bool:
    content = file_path.read_text(encoding="utf-8")
    lines = content.splitlines(keepends=True)

    import_indices: list[int] = []
    import_stmts: list[str] = []
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith("import "):
            import_indices.append(i)
            import_stmts.append(stripped)

    imports_needed: list[str] = []
    for fq_name, identifier in FQ_NAME_MAP.items():
        pkg = _package_of(fq_name)
        if _is_imported(import_stmts, fq_name, pkg):
            continue

        pattern = rf"\b{re.escape(identifier)}\b"
        found = False
        for i, line in enumerate(lines):
            stripped = line.strip()
            if stripped.startswith("import ") and fq_name in stripped:
                continue
            if stripped.startswith("package "):
                continue
            if re.search(pattern, line):
                found = True
                break

        if found:
            imports_needed.append(f"import {fq_name}\n")

    if not imports_needed:
        return False

    if import_indices:
        insert_idx = import_indices[-1] + 1
    else:
        # 没有 import 语句，放在 package 声明之后
        insert_idx = 0
        for i, line in enumerate(lines):
            if line.strip().startswith("package "):
                insert_idx = i + 1
                break
        imports_needed.insert(0, "\n")

    new_lines = lines[:insert_idx] + imports_needed + lines[insert_idx:]
    file_path.write_text("".join(new_lines), encoding="utf-8")
    print(f"  Modified: {file_path}")
    return True


def main() -> None:
    root = Path(__file__).resolve().parent.parent.parent

    for scan_dir in SCAN_DIRS:
        if not (root / scan_dir).exists():
            print(f"Warning: Directory not found: {root / scan_dir}")
            return

    total = 0
    modified = 0

    for scan_dir in SCAN_DIRS:
        print(f"\nScanning: {scan_dir}")
        for kt_file in sorted((root / scan_dir).rglob("*.kt")):
            total += 1
            if process_file(kt_file):
                modified += 1

    print(f"\nDone. Scanned {total} files, modified {modified}.")


if __name__ == "__main__":
    main()
