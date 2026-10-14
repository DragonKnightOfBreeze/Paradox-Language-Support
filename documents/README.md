# Maintainer Documentation

[By the dragon knight a coffee ☕](https://afdian.com/a/dk_breeze)

[English](README.md) | [中文](README_zh.md)

---

## Overview

This directory stores public-facing maintainer documentation of **Paradox Chronicle** (public portion).

Unlike end-user documentation in `docs/` or the root `README.md`, documents here are intended for **project maintainers and AI coding agents**. They record in-depth architectural specifications, historical Architecture Decision Records (ADRs), diagnostic post-mortems, profiling benchmarks, and development roadmaps.

### Language & Naming Policy

- **Content Language**: Primarily written in **Simplified Chinese** to maximize cognitive precision, depth, and maintenance efficiency for the core maintainer. Non-Chinese readers are warmly encouraged to use modern AI assistants or translation engines. English contributions to maintainer notes are welcome.
- **File System & Paths**: All file and directory names strictly adhere to **English kebab-case** (e.g., `snippet-match.md`, `roadmap.md`), completely avoiding URL-encoding glitches and anchor-breaking issues across GitHub, IDEs, and tools.

## Directory Structure & Categorization

```text
documents/
├── design/                 # Living system design specifications (UI, syntax abstractions, subsystems)
├── notes/                  # Core human-maintained knowledge (authoritative, actively maintained)
│   ├── adr.md              # ADR index and category catalog
│   ├── adr/                # Self-contained RFC-style Architecture Decision Records
│   ├── issues.md           # Problem tracking backlog and P1~Pnn hard bug index
│   ├── issues/             # P01~Pnn deep diagnostic post-mortems
│   ├── solutions.md        # Troubleshooting guides and workaround index
│   ├── solutions/          # Specific solution and pitfall guides
│   ├── analysis.md         # Long-term performance and memory profiling index
│   ├── analysis/           # Index latency and heap memory benchmark rounds
│   ├── assets/             # Associated stack traces, thread dumps, and logs
│   ├── roadmap.md          # Global roadmap and version-by-version dashboard
│   ├── guidelines.md       # Architecture conventions, module dependencies, and code style
│   ├── ideas.md            # Brainstorming backlog and long-term vision
│   └── cheatsheet.md       # Live templates, regular expressions, and CLI shortcuts
├── ai-notes/               # AI-assisted archive area (archived as-is, zero-maintenance)
│   ├── analysis.md         # Diagnostic analysis and domain mechanism index
│   ├── analysis/           # Snapshots of bug investigations and internal mechanics
│   ├── solutions.md        # AI-assisted troubleshooting solutions index
│   ├── solutions/          # Practical environment and build solution guides
│   ├── reports.md          # Assessment and status report index
│   ├── reports/            # Complexity evaluations, tech research, and potential alternative evaluations
│   ├── study.md            # External third-party technology study notes index
│   ├── study/              # Study notes on external SDKs (Gradle, MCP, LSP, Kotlin, etc.)
│   └── adr.md              # Draft or alternate architectural proposals index
├── changelogs/             # Archived historical release changelogs
└── reports/                # Periodic automated code and config statistics snapshots
```

### 1. design/ (System Design Specifications)

Living technical specifications for UI visual assets, syntax abstractions, and subsystem architectures. Co-authored by maintainers and AI assistants:

- [Icon Design](design/icon-design.md) - File types, AST nodes, and config rule icon color mappings
- [Color Scheme](design/color-scheme.md) - Lexical and semantic highlighting colors across all supported languages
- [Prompt Template Engine](design/prompt-template-engine.md) - Built-in AI prompt template syntax, directives, and lifecycle
- [Mod Import/Export](design/mod-import-export.md) - Mod dependency import/export architecture and format adaptors
- [Language Abstractions](design/language-abstractions.md) - Syntax and semantic model abstraction hierarchy
- [Terminology](design/terminology.md) - Domain term English-Chinese mapping and standard naming
- [Spellchecking](design/spellchecking.md) - Token splitting and grammar-level spellchecking strategy
- [Script Injection](design/script-injection.md) - Cross-language injection and dynamic config context resolution
- [Tool Integration: Tiger](design/tool-integration-tiger.md) - ck3-tiger validator integration architecture

### 2. notes/ (Maintainer Notes)

Authoritative knowledge base maintained directly by the core maintainer. **In principle, documents here must be written by humans** to preserve genuine cognitive rigor. Architecture Decision Records (ADRs) adopt a self-contained RFC style that remains immutable once implemented.

Key entry points and sibling indexes:
- [Guidelines](notes/guidelines.md) - Architectural layers, module dependency constraints, and coding conventions
- [Roadmap](notes/roadmap.md) - Version-by-version development progress, refactoring plans, and completed tasks
- [Ideas](notes/ideas.md) - Brainstorming pool and long-term backlog
- [Cheatsheet](notes/cheatsheet.md) - Live templates, regular expressions, and CLI shortcuts
- [Architecture Decision Records (ADR)](notes/adr.md) - Central catalog of self-contained architectural decision files
- [Problem Tracking (Issues)](notes/issues.md) - Active debugging backlogs and deep analyses for P1~Pnn hard bugs
- [Solutions](notes/solutions.md) - Environment configurations, proxy setup, and third-party library workarounds
- [Profiling Analysis](notes/analysis.md) - Long-term indexing throughput and heap memory benchmark rounds

### 3. ai-notes/ (AI Collaboration Archive)

A low-friction staging and archive area. Documents are **archived as-is** and **carry no continuous maintenance obligation** to stay synchronized with future code changes. Mature insights may be promoted to `design/` or `notes/adr/` as needed.

Sibling index entry points:
- [Analysis Index](ai-notes/analysis.md) - Incident diagnostics and internal mechanism analyses
- [Solutions Index](ai-notes/solutions.md) - Environment workarounds and build solutions
- [Reports Index](ai-notes/reports.md) - Project assessments, potential alternative evaluations, and tech research
- [Study Notes Index](ai-notes/study.md) - External third-party technology and platform SDK study notes
- [Architecture Proposals Index](ai-notes/adr.md) - AI-assisted technical pre-research and architectural proposals
