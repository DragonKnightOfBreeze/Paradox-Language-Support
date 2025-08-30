# GitHub Actions 学习笔记

## 概述

GitHub Actions 是 GitHub 提供的持续集成和持续交付 (CI/CD) 平台，可用于自动化构建、测试和部署流程。它支持跨平台工作流，可直接集成在 GitHub 仓库中。

## 核心概念

1. **工作流 (Workflow)**
   - 自动化流程的顶级组件
   - 存储在 `.github/workflows` 目录
   - 由 YAML 文件定义

2. **事件 (Events)**
   ```yaml
   on:
     push:
       branches: [main]
     pull_request:
       branches: [main]
     release:
       types: [released]
   ```

3. **任务 (Jobs)**
   - 在工作流中并行/顺序执行
   - 每个任务运行在独立环境中

4. **步骤 (Steps)**
   - 任务中的单个命令/操作
   - 可以是 shell 命令或预定义 action

## 最佳实践

### 缓存依赖
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v3
  with:
    path: ~/.gradle/caches
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle') }}
```

### 矩阵构建
```yaml
jobs:
  build:
    strategy:
      matrix:
        java: [17, 21]
    steps:
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java }}
```

## 工作流示例

### 智能版本更新
```yaml
name: Update Version

on:
  release:
    types: [created]

jobs:
  update-version:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Extract version
        run: |
          VERSION=${GITHUB_REF#refs/tags/v}
          echo "version=$VERSION" >> $GITHUB_ENV
      - name: Update gradle.properties
        run: echo "pluginVersion=${{ env.version }}" > gradle.properties
```

## 附加工作流建议

### 1. 代码质量检查

在每次 PR 时运行代码静态分析：

```yaml
name: Code Quality

on:
  pull_request:
    branches: [main, master]

jobs:
  lint:
    name: Run Linter
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Run ktlint
        run: ./gradlew ktlintCheck
```

### 2. 自动化版本更新

在创建新 release 时自动更新项目版本号：

```yaml
name: Update Version

on:
  release:
    types: [created]

jobs:
  update-version:
    name: Update Plugin Version
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        
      - name: Extract release version
        id: version
        run: |
          VERSION=${GITHUB_REF#refs/tags/v}
          echo "version=$VERSION" >> $GITHUB_OUTPUT
        
      - name: Update gradle.properties
        run: |
          echo "pluginVersion=${{ steps.version.outputs.version }}" > gradle.properties
        
      - name: Commit version update
        uses: stefanzweifel/git-auto-commit-action@v4
        with:
          commit_message: "Update plugin version to ${{ steps.version.outputs.version }}"
```

### 3. 依赖安全扫描

每周自动扫描依赖漏洞：

```yaml
name: Dependency Scan

on:
  schedule:
    - cron: '0 0 * * 1' # 每周一 UTC 时间午夜运行
  workflow_dispatch:

jobs:
  scan:
    name: Dependency Scan
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Run dependency check
        uses: dependency-check/action@v3
        with:
          project: 'Paradox-Language-Support'
          format: 'HTML'
          fail_on_cvss: '7'
```

### 4. 文档构建

在文档更新时自动构建并发布：

```yaml
name: Documentation

on:
  push:
    branches: [main, master]
    paths:
      - 'docs/**'
      - '.github/workflows/documentation.yml'

jobs:
  build-docs:
    name: Build Documentation
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        
      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: '3.10'
          
      - name: Install dependencies
        run: |
          python -m pip install --upgrade pip
          pip install mkdocs-material
          
      - name: Build docs
        run: mkdocs build
        
      - name: Deploy to GitHub Pages
        if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'
        uses: peaceiris/actions-gh-pages@v4
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./site

## 权限与安全

- **最小权限原则**：显式声明 `permissions`，严格限制 `GITHUB_TOKEN` 能力。
```yaml
permissions:
  contents: read
  pull-requests: write
```

- **固定 Action 版本**：优先使用 `@vX` 或 commit SHA，避免 `@master`。
- **机密管理**：使用 `secrets.*` 注入；避免直接回显到日志。

## 并发与取消

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

## 可复用工作流（Reusable Workflows）

```yaml
# .github/workflows/reusable.yml
name: Reusable Build
on: workflow_call

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: gradle/actions/setup-gradle@v3
      - run: ./gradlew build --stacktrace
```

```yaml
# 调用方 workflow
jobs:
  call-build:
    uses: ./.github/workflows/reusable.yml
```

## Gradle 缓存

```yaml
steps:
  - uses: actions/checkout@v4
  - uses: actions/setup-java@v4
    with:
      distribution: temurin
      java-version: '21'
  - name: Setup Gradle
    uses: gradle/actions/setup-gradle@v3
  - run: ./gradlew build -x test
```

## OIDC 到云（示例：AWS）

```yaml
permissions:
  id-token: write
  contents: read

steps:
  - uses: aws-actions/configure-aws-credentials@v4
    with:
      role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
      aws-region: ap-southeast-1
  - run: aws sts get-caller-identity
```

## 调试与本地测试

- **日志增强**：在仓库/环境 Secrets 中设置 `ACTIONS_STEP_DEBUG=true`。
- **actionlint**：静态检查工作流 YAML。
```yaml
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: reviewdog/action-actionlint@v1
```

- **本地运行**：使用 `act` 进行快速迭代测试。

## 发布触发与环境保护

```yaml
on:
  release:
    types: [released]

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production
    steps:
      - uses: actions/checkout@v4
      - run: echo "Deploy tag ${{ github.ref_name }}"
```

说明：`environment: production` 可启用保护规则（审批人、机密隔离）。

## JetBrains 插件发布（示例）

```yaml
name: Publish JetBrains Plugin

on:
  workflow_dispatch:
  release:
    types: [released]

jobs:
  publish:
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - uses: gradle/actions/setup-gradle@v3
      - name: Build & Sign
        env:
          PUBLISH_TOKEN: ${{ secrets.PUBLISH_TOKEN }}
          PRIVATE_KEY: ${{ secrets.PRIVATE_KEY }}
          PRIVATE_KEY_PASSWORD: ${{ secrets.PRIVATE_KEY_PASSWORD }}
          CERTIFICATE_CHAIN: ${{ secrets.CERTIFICATE_CHAIN }}
        run: |
          ./gradlew buildPlugin signPlugin --info
      - name: Publish
        env:
          PUBLISH_TOKEN: ${{ secrets.PUBLISH_TOKEN }}
        run: |
          ./gradlew publishPlugin --info
```

## 参考链接

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [预构建 Actions 市场](https://github.com/marketplace/actions)
- [GitHub Actions 工作流语法](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [GitHub Actions 备忘单](https://github.com/actions-cheatsheet)
- [高级工作流模式](https://github.com/actions/workflow-patterns)