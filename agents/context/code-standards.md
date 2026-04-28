---
created: 2026-04-28
updated: 2026-04-28
---

## General

- **Caching**: Prefer `CacheBuilder` (from `icu.windea.pls.core.util.CachesKt`); use `ConcurrentHashMap` only when insufficient
- **String interning**: Use `com.github.benmanes.caffeine.cache.Interner`
- **Cache strength**: Global caches → strong refs + size+TTL; large/config caches → soft refs
- **Indexing**: File-level analysis → `FileBasedIndex`; PSI-structure-dependent → `StubIndex`; ref-resolve-dependent → `FileBasedIndex`
- **Extension**: Extend via EP and config-driven mechanisms; avoid hardcoding game-specific behavior
- **Change scope**: Keep changes minimal and localized; prefer existing EPs to new ones
- **Config access**: `PlsFacade.getConfigGroup(project, gameType)` / `PlsFacade.getConfigGroup(gameType)`
- **Coroutine scope**: `PlsFacade.getCoroutineScope(project)` / `PlsFacade.getCoroutineScope()`
- **Config context**: `ParadoxConfigManager.getConfigContext(element)`
- **Matched configs**: `ParadoxConfigManager.getConfigs(element, options)`