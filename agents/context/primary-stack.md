---
created: 2026-04-28
updated: 2026-04-28
---

## General

| Layer | Technology | Notes |
|-------|-----------|-------|
| Language | **Kotlin** (primary) | Java only for Grammar-Kit generated code and rare legacy cases |
| Build | Gradle + IntelliJ Platform Gradle Plugin | `./gradlew` on Windows |
| SDK | IntelliJ Platform SDK | PSI-based, NOT LSP |
| Parser | Grammar-Kit | Generates Java lexer/parser; wrap in Kotlin |
| AI | LangChain4j | Localisation translation/polishing workflows |
| Testing | JUnit4 + IntelliJ Platform Test Framework | |
| JDK | 21 | `kotlin.jvmToolchain(21)` |