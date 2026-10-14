# 问题解决与避坑指南

## 概述

本目录记录 AI 协同排查中产出的各类针对构建签名、第三方库兼容、Node/Vite、测试环境等的具体避坑手册与解决方案。

## 索引

- [插件 jar 内置规则文件更新后读取仍为旧内容](solutions/bundled-configs-in-jar-stale-content.md)
- [何时优先使用 fastutil 中的集合](solutions/fastutil-collections-usage-guide.md)
- [Gradle Download 插件下载失败：SSLHandshakeException / PKIX path building failed](solutions/gradle-download-ssl-handshake-failure.md)
- [Gradle + JUnit4 测试无法读取 -D 系统属性](solutions/gradle-junit4-system-properties.md)
- [平台不再提供 Jackson 导致 IDEA 2026.3 EAP 兼容性问题（迁移到 kotlinx-serialization）（2026-09-30）](solutions/jackson-removal-in-idea-2026-3-eap.md)
- [nvm use 后 node -v 仍输出旧版本（NVM_SYMLINK 非符号链接导致）](solutions/nvm-symlink-node-version-issue.md)
- [Prism 自定义语法（SSR 扩展，避免 window.Prism 依赖）](solutions/prism-custom-syntax-ssr.md)
- [ProfilesSettings 持久化迁移到 SQLite（Ktorm + DbBackedStateMap）（2025-08-31）](solutions/profiles-settings-sqlite-migration.md)
- [在读操作中使用 withModalProgress 导致可能的死锁（IllegalStateException）](solutions/read-action-with-modal-progress-deadlock.md)
- [VuePress docs:dev 启动失败（Vite 7 与 Node 版本不兼容，crypto.hash is not a function）](solutions/vuepress-vite-crypto-hash-failure.md)
