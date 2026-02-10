from __future__ import annotations

import os
from dataclasses import dataclass
from typing import Iterable


@dataclass
class Stats:
    file_count: int = 0
    total_lines: int = 0
    min_lines: int = 0
    max_lines: int = 0
    total_code_lines: int = 0

    @property
    def avg_lines(self) -> float:
        if self.file_count == 0:
            return 0.0
        return self.total_lines / self.file_count


def iter_files(base_dir: str, extensions: Iterable[str]) -> Iterable[str]:
    for root, _, files in os.walk(base_dir):
        for filename in files:
            if any(filename.endswith(ext) for ext in extensions):
                yield os.path.join(root, filename)


def count_file_lines(file_path: str) -> tuple[int, int]:
    total_lines = 0
    code_lines = 0
    with open(file_path, "r", encoding="utf-8", errors="ignore") as handle:
        for line in handle:
            total_lines += 1
            if line.strip():
                code_lines += 1
    return total_lines, code_lines


def collect_stats(base_dir: str, extension: str) -> Stats:
    stats = Stats()
    for file_path in iter_files(base_dir, [extension]):
        total_lines, code_lines = count_file_lines(file_path)
        stats.file_count += 1
        stats.total_lines += total_lines
        stats.total_code_lines += code_lines
        if stats.file_count == 1:
            stats.min_lines = total_lines
            stats.max_lines = total_lines
        else:
            stats.min_lines = min(stats.min_lines, total_lines)
            stats.max_lines = max(stats.max_lines, total_lines)
    return stats


def print_stats(label: str, stats: Stats) -> None:
    print(f"- {label}")
    print(f"  - 文件数量: {stats.file_count}")
    print(f"  - 总行数: {stats.total_lines}")
    print(f"  - 最小行数: {stats.min_lines}")
    print(f"  - 最大行数: {stats.max_lines}")
    print(f"  - 平均行数: {stats.avg_lines:.2f}")
    print(f"  - 代码行数(非空行): {stats.total_code_lines}")


def main() -> None:
    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
    cwt_root = os.path.join(repo_root, "cwt")
    if not os.path.isdir(cwt_root):
        print("未找到 cwt 目录，无法统计")
        return

    print("CWT 目录统计结果:")
    for entry in sorted(os.scandir(cwt_root), key=lambda item: item.name):
        if not entry.is_dir():
            continue
        relative_path = os.path.join("cwt", entry.name)
        stats = collect_stats(entry.path, ".cwt")
        print(f"\n目录: {relative_path}")
        print_stats("CWT (.cwt)", stats)


if __name__ == "__main__":
    main()
