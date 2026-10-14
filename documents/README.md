# Maintainer Documentation System

[English](README.md) | [中文](README_zh.md)

## Overview

This directory stores all internal maintainer documentation for the **Paradox Chronicle** plugin.

Unlike public-facing user documentation in `docs/` or the root `README.md`, documents here are intended for **project core authors, long-term maintainers, and AI coding agents**. They record in-depth architectural specifications, historical Architecture Decision Records (ADRs), diagnostic post-mortems, profiling benchmarks, and development roadmaps.

### Language & Naming Policy

- **Content Language**: Primarily written in **Simplified Chinese** to maximize cognitive precision, depth, and maintenance efficiency for the core maintainer. Non-Chinese readers are warmly encouraged to use modern AI assistants or translation engines. English contributions to maintainer notes are welcome.
- **File System & Paths**: All file and directory names strictly adhere to **English kebab-case** (e.g., `snippet-match.md`, `roadmap.md`), completely avoiding URL-encoding glitches and anchor-breaking issues across GitHub, IDEs, and tools.

## Directory Structure & Categorization

```text
documents/
├── design.md               # Overview and index for system design specifications
├── design/                 # Living system design specifications (co-authored with AI)
├── notes/                  # [Core Human-Maintained Area] (Authoritative, actively updated)
│   ├── adr.md              # ADR index and category catalog
│   ├── adr/                # 68 self-contained RFC-style Architecture Decision Records
│   ├── issues.md           # Problem tracking backlog and P1~P22 hard bug index
│   ├── issues/             # P01~P22 deep diagnostic post-mortems
│   ├── solutions.md        # Troubleshooting guides and workaround index
│   ├── solutions/          # 12 specific solution and pitfall guides
│   ├── analysis.md         # Long-term performance and memory profiling index
│   ├── analysis/           # Index latency and heap memory benchmark rounds
│   ├── assets/             # Associated stack traces, thread dumps, and logs
│   ├── roadmap.md          # Global roadmap and version-by-version dashboard
│   ├── guidelines.md       # Architecture conventions, module dependencies, and code style
│   ├── ideas.md            # Brainstorming backlog and long-term vision
│   └── cheatsheet.md       # Live templates, regular expressions, and CLI shortcuts
├── ai-notes/               # [AI-Assisted Archive Area] (Archived as-is, zero-maintenance)
│   ├── analysis.md         # Diagnostic analysis and domain mechanism index
│   ├── analysis/           # Snapshots of specific bug investigations and internal mechanics
│   ├── solutions.md        # AI-assisted troubleshooting solutions index
│   ├── solutions/          # 10 practical environment and build solution guides
│   ├── reports.md          # Assessment and status report index
│   ├── reports/            # Complexity evaluations, tech research, and test coverage
│   ├── study.md            # External third-party technology study notes index
│   ├── study/              # Study notes on external SDKs (Gradle, MCP, LSP, Kotlin, etc.)
│   └── adr/                # Draft or alternate architectural proposals
├── changelogs/             # Archived historical release changelogs
└── reports/                # Periodic automated code and config statistics snapshots
```

### 1. design/ (System Design Specifications)
Living technical specifications for UI visual assets, syntax abstractions, and subsystem architectures. Co-authored by maintainers and AI assistants.

### 2. notes/ (Core Human-Maintained Knowledge)
Authoritative knowledge base maintained directly by the core maintainer. Architecture Decision Records (ADRs) adopt a **self-contained RFC style** that remains immutable once implemented. Each subfolder features a sibling index file (e.g., `adr.md` for `adr/`) for clean navigation.

### 3. ai-notes/ (AI Collaboration Archive)
A low-friction staging and archive area. Documents are **archived as-is** and **carry no continuous maintenance obligation** to stay synchronized with future code changes. Mature insights may be promoted to `design/` or `notes/adr/` as needed.

## Key Long-Term Documents

For direct access to foundational, long-term documentation, refer to the following key entry points:

### Dashboards & Guidelines
- [Roadmap](notes/roadmap.md) - Version-by-version development progress, refactoring plans, and completed tasks
- [Guidelines](notes/guidelines.md) - Architectural layers, module dependency constraints, and coding conventions
- [Ideas](notes/ideas.md) - Brainstorming pool and long-term backlog
- [Cheatsheet](notes/cheatsheet.md) - Live templates, regular expressions, and CLI shortcuts

### Sibling Indexes
- [Architecture Decision Records (ADR)](notes/adr.md) - Central catalog of 68 self-contained architectural decision files
- [Problem Tracking (Issues)](notes/issues.md) - Active debugging backlogs and deep analyses for P1~P22 hard bugs
- [Solutions](notes/solutions.md) - Environment configurations, proxy setup, and third-party library workarounds
- [Profiling Analysis](notes/analysis.md) - Long-term indexing throughput and heap memory benchmark rounds
- [Design Specifications](design.md) - Central index for all system-level design specifications

### Primary Design Specs
- [Icon Design](design/icon-design.md) - File types, AST nodes, and config rule icon color mappings
- [Color Scheme](design/color-scheme.md) - Lexical and semantic highlighting colors across all supported languages
- [Prompt Template Engine](design/prompt-template-engine.md) - Built-in AI prompt template syntax, directives, and lifecycle
