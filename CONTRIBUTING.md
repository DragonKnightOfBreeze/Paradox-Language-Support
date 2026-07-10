# Contributing

<!-- As recorded in widely circulated ancient tales left by precursors, this book of prophecy records the truth and reality of the world. -->

## Welcome

Thank you for your interest in writing **Paradox Chronicle**.

This is a rather special project: the IntelliJ IDEA plugin designed for mod developers of Paradox Interactive games, built on PSI-based deep semantic analysis and driven by its own CWT config system.

Although it may not be immediately noticeable at first glance, this project indeed has a fairly large codebase and a high level of complexity.
It contains over 100k lines of Kotlin production code, along with tests, config files, and documentation.
Its scope spans the IntelliJ Platform, the Paradox modding domain, and a fair amount of in-house architecture.
That makes it both an interesting playground and a genuinely challenging codebase to work in.

This guide explains **what kinds of contributions are welcome**, **what to read before you start**, and **how to actually get changes in**.
It assumes you already have programming experience, so it favors keywords, links and short steps over step-by-step tool tutorials.
If a tool or term is unfamiliar, treat the mentions here as search hints.

As you embark on this journey, keep serious, cautious, and curious.

## Before You Start

Please read the relevant material before opening an issue or a pull request. A little reading up front saves a lot of back-and-forth later.

- Project overview and usage: [`README.md`](README.md) / [`README_zh.md`](README_zh.md)
- General guidance for agents: [`AGENTS.md`](AGENTS.md)
- Maintainer documents (written in Simplified Chinese): [`documents`](documents)
- Reference documentation: [`docs`](docs)
  - [Reference Documentation Website](https://windea.icu/Paradox-Language-Support)
  - The config system: [`docs/en/config.md`](docs/en/config.md)
  - Language syntax reference: [`docs/en/ref-syntax.md`](docs/en/ref-syntax.md)
  - Config format reference: [`docs/en/ref-config-format.md`](docs/en/ref-config-format.md)
- IntelliJ Platform background:
  - [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
  - Getting started from the [Quick Start Guide](https://plugins.jetbrains.com/docs/intellij/plugins-quick-start.html) and the [Developing a Plugin](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html) docs
  - In particular the [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) docs and the [Custom Language Support](https://plugins.jetbrains.com/docs/intellij/custom-language-support.html) docs
- Upstream CWT tooling and configs:
  - [CWTools](https://github.com/cwtools/cwtools)
  - The [respective config repositories](cwt/README.md)

## What We Accept

We gladly accept the following kinds of contributions:

- **Non-core code** - Improvements and additions to plugin features, capabilities and compatibility, typically built on top of existing extension points (EPs) and the config-driven mechanisms. See [Contributing Code](#contributing-code).
- **Documentation** - Fixes and improvements to general docs (e.g. `README.md`), reference docs (the `docs` directory), and config-repository docs. Reference manuals especially benefit from corrected factual errors and added detail.
- **Config files** - Additions and fixes to CWT config files. Because the plugin is config-driven, this is one of the most effective ways to improve support for a specific game. See [Contributing Config Files](#contributing-config-files).

These are well-suited for community work, with existing references and even AI assistance — provided the result is reviewed, validated, tested and (when needed) debugged before submission.

## What We Don't Accept (Directly)

Some parts of the codebase are foundational, and uncoordinated changes there carry an unacceptable cost.
For the following areas, **please do not open a direct pull request** without prior discussion and explicit approval:

- **Core semantic logic** - semantic matching and resolution (`icu.windea.pls.lang.match`, `icu.windea.pls.lang.resolve`, and the surrounding query/resolve machinery).
- **Base domain models** - the foundational models for configs, definitions, scopes, expressions, and the config group system.
- **Base architecture** - the extension point architecture, the code injection subsystem, indexing strategy, and similar infrastructure.

This is not a closed door. Feedback, bug reports, design proposals and questions about these areas are very welcome - through [GitHub Issues](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues), [Discord](https://discord.gg/vBpbET2bXT), or email.
If a discussion converges on a concrete plan, a PR into these areas can absolutely follow.
The goal is to keep the foundation consistent and correct, not to discourage you.

## Contributing Code

The codebase is large and the domain is intricate, so contributing code is challenging, but far from impossible.
The note is to face the complexity when necessary: you will have to explore on your own and even think a lot, if you want to go deeper into this journey.
The trick is to extend rather than rewrite: follow existing reference points like architecture, code conventions, code implementations, instead of moving forward without a clue.

### Setup

The project uses **Gradle** with the **IntelliJ Platform Gradle Plugin**.

- Requirements: **JDK 21** (the build uses `kotlin.jvmToolchain(21)`) and the Gradle wrapper.
- Run the IDE for debugging: `./gradlew runIde`
- Build the plugin ZIP: `./gradlew buildPlugin`
- Run tests: `./gradlew test`

On Windows PowerShell, prefer the wrapper and prefix it when needed: `.\gradlew <task>`.

The local config repos used are in the `cwt/<repoDir>` directory, and there are also some useful scripts in the `cwt/scripts` directory.
Typically, after configuring the basic development environment, you should clone the local config repos into here, and update as needed during development.

The plugin bundles CWT configs into the plugin JAR under `config/<gameTypeId>`.
If missing (common in CI), Gradle can download ZIPs and unzip them into `build/generated/cwt/<repoDir>`.

### A Suggested Workflow

1. **Find a seam.** Most language features are registered in `src/main/resources/META-INF/plugin.xml` (and the including XML configuration files). Inspecting these registrations, combined with keyword searches, is the fastest way to locate where a feature is wired up.
2. **Follow an existing example.** When adding a feature, find a sibling EP implementation or component implementation and mirror its shape.
3. **Prefer semantic navigation.** When exploring symbols, IDE/index-based navigation (find references, go to definition, find implementations, type hierarchy) is far more accurate than plain text search for this PSI-heavy codebase.
4. **Add or update tests.** Distinguish unit tests (pure components, no IntelliJ API) from integration tests (PSI / index / config-driven resolution). Config-driven features usually need a simulated game/mod context.
5. **Verify before submitting.** Run targeted tests during development and a broader pass before finishing, run and debug IDE when necessary. Running IDE inspections on the files you touched is a good habit.

Keep changes minimal and localized. A small, well-scoped, well-tested PR that improves one feature or one compatibility case is much easier to accept than a sprawling one.

### Good Starting Points

- Adding or improving a single language feature for a specific construct.
- Improving compatibility with a specific game, IDE version, or optional plugin dependency (for example, the Markdown plugin).
- Adding default EP implementations where the architecture already anticipates them (for example, additional script expression matchers and supports).

## Contributing Config Files

Config files define the semantic information (definitions, modifiers, scopes, effects, triggers, etc.) that powers config-driven features.
Keeping them current with each game version directly improves the experience for everyone playing that game.

- Each game has a **Built-in** repository (used by the plugin, with plugin-specific additions and extensions) and a **Vanilla** repository (the upstream CWTools source). The full mapping and the current maintenance status per game live in [`CONTRIBUTORS.md`](CONTRIBUTORS.md) and [`cwt/README.md`](cwt/README.md).
- Before editing configs, read the [config system documentation](docs/en/config.md), the [syntax reference](docs/en/ref-syntax.md), and the [config format reference](docs/en/ref-config-format.md). The plugin's CWT format is largely consistent with CWTools, with some improvements and extensions.
- Config changes are best validated by running the plugin (`./gradlew runIde`) against a real mod/game setup and confirming that completion, inspections and documentation behave as intended.
- As an alternative, you may also consider keeping in dev IDE, and using global-local or project-local config files to validate config changes.
- For games actively maintained upstream, please consider whether a fix belongs in the Vanilla repository as well, so the two stay in sync.

## Contributing Documentation

Documentation falls into three groups:

- **General documentation** - e.g. `README.md`, `CONTRIBUTORS.md`, this file.
- **Reference documentation** - the `docs` directory (the published reference manuals).
- **Maintainer documentation** - the `documents` directory.

These docs inevitably contain errors, gaps and stale details. Corrections and additions are welcome, especially to the reference documentation.

## On AI Assistance

This is a complex and serious project that demands high accuracy and real extensibility. It is **not** a place for blind "vibe-coding": a plausible-looking change that subtly breaks semantic matching or the config system has a real, user-facing cost.

That said, AI assistance is genuinely viable for parts of this project, and we don't reject it.
There is a lot of existing material to anchor against - documentation, reference manuals, configuration XML, code implementations, and the CWT config repositories - which makes AI-assisted work on documentation, config files and non-core features practical.

If you use AI, please hold yourself to a few standards:

- **Human review is mandatory.** Treat AI output as a draft, not a result. Read it, understand it, revise it, and verify it (compile, test, and when needed, debug) before submitting.
- **Preserve conceptual consistency.** Match the existing naming conventions, package organization, domain terminology, and the established style of the surrounding code and docs. Consistency here is worth more than cleverness.
- **Stay within accepted areas.** The same boundaries apply: AI does not make core-code changes acceptable. Use the discussion path for foundational areas.

## Submitting Changes

- Open issues for bugs, feature requests, and proposals (especially for anything touching the core areas above).
- For pull requests, target the [plugin repository](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) (this project) or the relevant [config repository](cwt/README.md).
- Keep each PR focused, describe what it changes and why, and mention how you verified it.
- Make sure tests pass (`./gradlew test`, or a targeted run) before requesting review.

If you're unsure whether an idea fits, ask first - a short discussion on Issues or Discord is the surest way to avoid wasted effort.

Welcome aboard, and happy writing.