# Stellaris模组开发笔记

## 游戏本体

### 基础信息
- **开发商**：Paradox Development Studio
- **发行时间**：2016年5月9日
- **游戏类型**：太空4X策略游戏

### 官方资源
- [Steam商店页面](https://store.steampowered.com/app/281990/Stellaris/)
- [官方Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
- [官方论坛](https://forum.paradoxplaza.com/forum/forums/stellaris.870/)

## 模组开发

### 核心概念
1. **文件结构**：`common/` `events/` `gfx/` 等目录
2. **脚本语言**：Paradox Script (类似Clausewitz引擎专用语言)
3. **作用域系统**：country planet pop 等作用域链

### 开发工具
1. **CWTools**：VSCode 扩展，提供语法检查和自动完成
   - [GitHub 仓库](https://github.com/cwtools/cwtools)
   - [VSCode 市场](https://marketplace.visualstudio.com/items?itemName=tboby.cwtools)
2. **Paradox Language Support (PLS)**：IntelliJ IDEA 插件，支持高级功能
   - [GitHub 仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)
3. **其他工具**：
   - [Irony ModManager](https://github.com/agc93/irony)：模组管理工具
   - [Stellaris Modding Utility](https://github.com/cwtools/cwtools-vscode)：脚本调试工具

### 开发资源
- [模组制作指南](https://stellaris.paradoxwikis.com/Modding)
- [脚本API参考](https://stellaris.paradoxwikis.com/Modding#Script_documentation)
- [CWTools 文档](https://cwtools.io/)
- [PLS 文档](https://dragonknightofbreeze.github.io/Paradox-Language-Support/)
