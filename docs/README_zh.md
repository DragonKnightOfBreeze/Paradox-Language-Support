# 参考文档站点 (docs/)

[English](README.md) | [中文](README_zh.md)

---

## 概述

本目录存放 **Paradox Chronicle** 面向广大模组开发者和终端用户的**公开参考文档站点源码**。

文档站点基于 **VuePress 2** 构建，并自动化部署至 [GitHub Pages (https://windea.icu/Paradox-Language-Support)](https://windea.icu/Paradox-Language-Support)。

## 目录结构

- `docs/zh/` - 简体中文用户文档（快速开始、核心功能、规则系统、高级特性等）
- `docs/en/` - 英文用户文档（Corresponding English manuals）
- `docs/assets/` - 用户手册中引用的界面截图与静态图片
- `docs/.vuepress/` - VuePress 2 站点配置、自定义主题、PrismJS/Shiki 语法高亮扩展

## 关键核心参考手册

- [语法参考手册 (ref-syntax.md)](zh/ref-syntax.md) - Paradox Script、Localisation、CSV 及 CWT 语言的详细语法规范
- [规则格式参考手册 (ref-config-format.md)](zh/ref-config-format.md) - CWT 规则文件的格式规范、数据类型与规则指令参考
- [开发资源与参考 (ref-resources.md)](zh/ref-resources.md) - 官方开发文档、社区工具链接与参考资源

## 本地开发与预览

确保已安装 Node.js 与 pnpm：

```bash
cd docs
pnpm install
pnpm dev
```

本地服务启动后，默认可通过浏览器访问 `http://localhost:8080` 实时预览更改。

构建静态站点：

```bash
pnpm build
```

## 注意事项

- **请勿向本目录提交内部维护者笔记、架构决策草稿或技术调试记录**。
- 关于插件本身的内部实现细节、架构决策（ADR）、代码规范与深层技术排查，请参阅维护者文档目录：`documents/`。
