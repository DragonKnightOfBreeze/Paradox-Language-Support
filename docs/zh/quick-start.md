# 快速开始

## 使用

* 通过IDE打开你的模组根目录。
* 打开模组描述符文件，即根目录下的`descriptor.mod`（对于VIC3则是`.metadata/metadata.json`）。
* 点击位于编辑器右上角的悬浮工具栏中的模组配置按钮。
* 配置模组的游戏类型、游戏目录以及额外的模组依赖。
* 点击确定按钮完成配置，然后等待IDE索引完成。
* 开始你的模组编程之旅吧！

## 提示

* 如果需要进行全局搜索，请参考以下方式：
  * 点击`Ctrl Shift R`或者`Ctrl Shift F`在当前项目、目录或者指定作用域中搜索。
  * 点击`Shift Shift`查找文件、定义、封装变量以及其他各种符号。
* 如果需要进行代码导航，请参考以下方式：
  * 按住`Ctrl`并点击目标位置，从而导航到目标的声明或使用处。
  * 按住`Ctrl Shift`并点击目标位置，从而导航到目标的类型声明处。
  * 按住`Alt`并点击目标位置，从而导航到目标对应的CWT规则的声明处。
  * 按住`Shift Alt`并点击目标位置，从而导航到目标定义的相关本地化的声明处。
  * 按住`Ctrl Shift Alt`并点击目标位置，从而导航到目标定义的相关图片的声明处。
  * 点击`Navigate`或者编辑器右键菜单中的`Go To`，选择要导航到的目标。
  * 点击`Navigate > Definition Hierarchy`，打开定义层级窗口，从而查看某一类型的定义的实现关系。
  * 点击`Navigate > Call Hierarchy`，打开调用层级窗口，从而查看定义、本地化、封装变量等的调用关系。
  * 点击`Alt 1`或者`Project`工具窗口，打开项目视图面板，然后点击左上角的`Project > Paradox Files`，从而查看汇总后的游戏与模组文件。
  * 点击`Alt 1`或者`Project`工具窗口，打开项目视图面板，然后点击左上角的`Project > CWT Config Files`，从而查看汇总后的CWT规则文件。
* 如果需要进行全局代码检查，请参考以下方式：
  * 点击`Alt 6`或者`Problems`工具窗口，打开问题面板，然后查看当前文件存在的问题，或者进行整个项目的全局代码检查。
  * 点击`Code > Inspect Code...`，进行整个项目的全局代码检查。
  * 完成代码检查后，IDE将会在问题面板中显示详细的检查结果。
* 如果需要更改模组类型、游戏目录、模组依赖等设置，请通过以下方式之一打开模组设置对话框：
  * 点击`Settings > Languages & Frameworks > Paradox Language Support`，可配置默认的游戏目录。
  * 点击位于页面右上方的编辑器悬浮工具栏中的蓝色齿轮图标。
  * 在编辑器中打开右键菜单，点击`Paradox Language Support > Open Mod Settings...`。
  * 点击`Tools > Paradox Language Support > Open Mod Settings...`。
* 如果需要更改插件的全局设置，请参考以下方式：
  * 点击`Settings > Languages & Frameworks > Paradox Language Support`，打开插件的设置页面。
* 如果在使用过程中遇到意外问题，请尝试通过以下方式解决：
  * 更新IDE和插件到最新版本。
  * 如果可能与IDE索引有关，请尝试重建索引并重启IDE。（点击`File > Invalidate Caches... > Invalidate and Restart`）
  * 如果可能与插件内置的规则有关，请尝试[编写自定义的规则文件](config.md#writing-cwt-config-files)。
  * 如果可能与插件的配置有关，请尝试删除插件的配置文件。（`paradox-language-support.xml`，如果不知道具体位置，请使用[Everything](https://www.voidtools.com)）
  * 通过GitHub、Discord等渠道进行反馈。

## 已知问题

* 对Stellaris的某些黑魔法般的语言特性的支持尚不完善。
* 对Stellaris以外的游戏的支持尚不完善。
