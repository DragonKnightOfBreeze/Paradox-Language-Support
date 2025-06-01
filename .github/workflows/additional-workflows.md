# 额外 GitHub Actions 工作流建议

## 1. 代码质量检查

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

## 2. 自动化版本更新

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

## 3. 依赖安全扫描

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

## 4. 文档构建

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
```
