# 快速开始

## 使用步骤

1. 在 IDE 中打开你的模组根目录。
2. 打开模组描述符文件（`descriptor.mod`，VIC3 中为 `.metadata/metadata.json`）。
3. 点击编辑器右上角的悬浮工具栏中的 *模组配置* 按钮。
4. 配置模组的游戏类型、游戏目录及所需的模组依赖。
5. 确认配置，等待 IDE 完成索引。
6. 开始你的模组开发之旅吧！

## 实用技巧

- **全局搜索**：
  - 使用 `Ctrl + Shift + R` 或 `Ctrl + Shift + F` 在当前项目、目录或指定查询作用域中搜索。
  - 使用 `Shift + Shift` 快速查找文件、定义、封装变量及其他符号。
- **代码导航**：
  - 使用 `Ctrl + 点击` 跳转到目标的声明或使用位置。
  - 使用 `Ctrl + Shift + 点击` 跳转到目标的类型声明。
  - 使用 `Alt + 点击` 跳转到目标的相关规则的声明。
  - 使用 `Shift + Alt + 点击` 跳转到目标相关本地化的声明。
  - 使用 `Ctrl + Shift + Alt + 点击` 跳转到目标的相关图片的声明。
  - 通过 `Navigate` 菜单（或者编辑器右键菜单中的 `Go To` 选项）快速定位。
  - 使用 `Navigate > Definition Hierarchy` 打开定义的类型层级窗口，从而查看特定类型的定义。
  - 使用 `Navigate > Call Hierarchy` 打开定义的调用层级窗口，从而查看定义、本地化、封装变量等的调用关系。
  - 在项目面板中选择 `Paradox Files` 视图，浏览汇总后的游戏与模组文件。
  - 在项目面板中选择 `CWT Config Files` 视图，浏览汇总后的规则文件。
- **代码检查**：
  - 在问题面板中查看当前文件的问题。
  - 使用 `Code > Inspect Code…` 执行全局代码检查，并在问题面板中查看详细报告。
- **设置修改**：
  - 可通过以下方式打开插件的全局设置页面：
    - 点击设置页面中的 `Languages & Frameworks > Paradox Language Support`。
  - 可通过以下方式打开模组设置对话框：
    - 点击编辑器右上角的悬浮工具栏中的蓝色齿轮图标。
    - 点击编辑器右键菜单中的 `Paradox Language Support > Open Mod Settings...`。
    - 点击主菜单中的 `Tools > Paradox Language Support > Open Mod Settings...`。
  - 可在全局设置中修改偏好语言环境、默认游戏类型、默认游戏目录等配置，以及其他功能细节。
  - 可在模组设置中调整游戏目录、模组依赖等配置。
- **问题排查**：
  - 确保 IDE 和插件均为最新版本。
  - 如果问题可能与索引有关，可尝试[清除缓存并重启 IDE](https://www.jetbrains.com/help/idea/invalidate-caches.html)。
  - 如果问题可能与规则有关，可尝试[编写自定义的规则文件](https://windea.icu/Paradox-Language-Support/zh/config.html#write-cwt-config-files)。
  - 如果问题可能与插件配置有关，可尝试删除插件的配置文件（`paradox-language-support.xml`，推荐使用 [Everything](https://www.voidtools.com) 搜索定位）。
  - 欢迎通过 GitHub、Discord 等渠道反馈问题。

## 已知限制

- 对 Stellaris 中的部分复杂语言特性的支持仍在完善中。
- 对非 Stellaris 游戏中的特有语言特性的支持尚不完整，欢迎反馈与贡献。
- 目前仅为 Stellaris 和 Victoria 3 提供了较为完善的内置规则文件，欢迎提交 Pull Request。