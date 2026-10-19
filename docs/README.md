# Reference Documentation

[By the dragon knight a coffee ☕](https://afdian.com/a/dk_breeze)

[English](README.md) | [中文](README_zh.md)

---

## Overview

This directory contains the source code of the public-facing reference documentation site for **Paradox Chronicle**, intended for mod developers and plugin users.

The site is built with **VuePress 2** and automatically deployed to [GitHub Pages (https://windea.icu/Paradox-Language-Support)](https://windea.icu/Paradox-Language-Support).

## Directory Structure

- `docs/zh/` - Simplified Chinese user manuals (Quick Start, Core Features, Config System, Advanced Features, etc.)
- `docs/en/` - Corresponding English user manuals
- `docs/assets/` - Screenshots, diagrams, and static assets referenced in manuals
- `docs/.vuepress/` - VuePress 2 configuration, custom theme, PrismJS/Shiki syntax highlighters

## Key Reference Manuals

- [Language Syntax Reference (ref-syntax.md)](en/ref-syntax.md) - Detailed syntax specifications for Paradox Script, Localisation, CSV, and CWT
- [Config Format Reference (ref-config-format.md)](en/ref-config-format.md) - CWT config file format rules, data types, and directive references
- [Developer Resources (ref-resources.md)](en/ref-resources.md) - Official documentation, community tools, and external reference links

## Local Development & Preview

Ensure Node.js and pnpm are installed:

```bash
cd docs
pnpm install
pnpm dev
```

Once running, navigate to `http://localhost:8080` in your browser for real-time live preview.

To build the static site:

```bash
pnpm build
```
