# Dependabot 学习笔记

## 概述

Dependabot 是 GitHub 提供的依赖管理工具，可自动监控项目依赖并更新至最新版本，同时提供安全漏洞警报。支持多种包管理器和工作流集成。

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

## 进阶配置

```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
      timezone: "Asia/Shanghai"
      day: "sunday"
    open-pull-requests-limit: 10
    target-branch: "main"
    labels: ["dependencies", "automerge-candidate"]
    reviewers: ["your-github-id"]
    assignees: ["your-github-id"]
    rebase-strategy: "auto"
    commit-message:
      prefix: "deps"
      include: "scope"
    ignore:
      - dependency-name: "org.slf4j:slf4j-*"
        versions: ["1.7.x"]

  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "monthly"
```

说明：
- **限制 PR 数量**：`open-pull-requests-limit`
- **统一分支**：`target-branch`
- **提交消息规范**：`commit-message`
- **按需忽略**：`ignore`

## PR 管理与自动合并

- 建议仅对安全补丁和补丁级（patch）版本启用自动合并。
- 通过标签与语义化版本判断合并条件。

### 使用 `dependabot/fetch-metadata` 自动合并 Patch 版本

```yaml
name: Auto-merge Dependabot PRs

on:
  pull_request_target:
    types: [opened, edited, reopened, synchronize]

permissions:
  contents: write
  pull-requests: write

jobs:
  automerge:
    if: github.actor == 'dependabot[bot]'
    runs-on: ubuntu-latest
    steps:
      - name: Dependabot metadata
        id: meta
        uses: dependabot/fetch-metadata@v2
      - name: Enable auto-merge for patch updates
        if: contains(steps.meta.outputs.update-type, 'version-update:semver-patch')
        env:
          PR_URL: ${{ github.event.pull_request.html_url }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          gh pr merge --auto --merge "$PR_URL"
```

注意：使用 `pull_request_target` 时务必限定 `github.actor == 'dependabot[bot]'` 以降低风险。

## Gradle 与 GitHub Actions 生态

- 配置 Gradle 与 GitHub Actions 两个 `package-ecosystem`，统一由 Dependabot 维护。
- 对 Gradle，建议集中版本到 Version Catalogs（`libs.versions.toml`），方便审阅与回滚。
- 对 GitHub Actions，固定到主版本（如 `actions/checkout@v4`）。

## 私有注册表（示例：Maven）

```yaml
registries:
  my-maven:
    type: maven-repository
    url: https://maven.example.com/repository/releases
    username: ${{ secrets.MY_MAVEN_USER }}
    password: ${{ secrets.MY_MAVEN_TOKEN }}

updates:
  - package-ecosystem: "maven"
    directory: "/"
    registries: ["my-maven"]
    schedule:
      interval: "weekly"
```

## 与工作流联动建议

- 在 CI 中为依赖变更增加更严格的测试（回归、兼容性）。
- 对带有 `dependencies` 标签的 PR，增加额外校验（如插件签名/验签）。

## 常见问题与排错

- **PR 未创建**：检查目录路径是否正确、包管理器是否支持、仓库是否为私有且未配置凭据。
- **PR 过多**：使用 `groups` 合并低风险更新；提高 `interval` 至 monthly；限制 `open-pull-requests-limit`。
- **提交未能合并**：确认 `required checks` 通过以及 auto-merge 条件满足（标签、更新类型）。
- **安全警报未触发**：在仓库设置中启用 Dependabot Alerts 与 Security updates；定期检查安全中心（Security > Dependabot）。

## 参考链接

- [Dependabot 文档](https://docs.github.com/en/code-security/dependabot)
- [配置选项参考](https://docs.github.com/en/code-security/dependabot/dependabot-version-updates/configuration-options-for-the-dependabot.yml-file)
- [安全更新指南](https://docs.github.com/en/code-security/dependabot/working-with-dependabot/keeping-your-dependencies-updated-automatically)
- [Dependabot 核心功能](https://github.blog/2020-06-01-keep-all-your-packages-up-to-date-with-dependabot/)
- [依赖管理最佳实践](https://docs.github.com/en/get-started/using-github/managing-security-vulnerabilities)