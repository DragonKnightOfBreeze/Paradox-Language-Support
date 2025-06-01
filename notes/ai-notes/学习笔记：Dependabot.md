# Dependabot 学习笔记

## 概述

Dependabot 是 GitHub 提供的依赖管理工具，可自动监控项目依赖并更新至最新版本，同时提供安全漏洞警报。支持多种包管理器和工作流集成。

**官方参考链接**：
- [Dependabot 文档](https://docs.github.com/en/code-security/dependabot)
- [配置选项参考](https://docs.github.com/en/code-security/dependabot/dependabot-version-updates/configuration-options-for-the-dependabot.yml-file)
- [安全更新指南](https://docs.github.com/en/code-security/dependabot/working-with-dependabot/keeping-your-dependencies-updated-automatically)

**其他参考链接**：
- [Dependabot 核心功能](https://github.blog/2020-06-01-keep-all-your-packages-up-to-date-with-dependabot/)
- [依赖管理最佳实践](https://docs.github.com/en/get-started/using-github/managing-security-vulnerabilities)

## 核心功能

1. **依赖更新**
   - 自动检测依赖新版本
   - 创建 PR 更新依赖

2. **安全警报**
   - 扫描漏洞依赖
   - 提供修复建议

3. **多生态支持**
   ```yaml
   ecosystems:
     - npm
     - gradle
     - maven
     - github-actions
   ```

## 基础配置

`.github/dependabot.yml` 示例：
```yaml
version: 2
updates:
  - package-ecosystem: "npm"
    directory: "/"
    schedule:
      interval: "weekly"
    labels:
      - "dependencies"

  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "monthly"
```

## 高级技巧

### 忽略特定更新
```yaml
ignore:
  - dependency-name: "eslint"
    versions: ["6.x", "7.x"]
```

### 分组更新
```yaml
groups:
  eslint:
    patterns: ["eslint*", "@types/eslint*"]
```

## 安全警报处理

1. **自动修复**：
   ```yaml
   vulnerability-alerts:
     enabled: true
   ```

2. **手动审查**：
   - 检查依赖树影响范围
   - 测试兼容性后再合并
