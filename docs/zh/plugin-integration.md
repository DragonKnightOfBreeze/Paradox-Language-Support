# 插件集成

## 集成**Translation**插件

当同时安装且启用了[Translation](https://github.com/YiiGuxing/TranslationPlugin)插件时，PLS将会额外提供以下功能：

### 翻译文档内容

当光标位于定义的名字、本地化的名字等位置时，
可以通过点击并选择快速文档弹出窗口中的`Translate Documentation`图标按钮，
或者点击右键菜单中的`Translate Documentation`选项翻译文档内容。

![](../images/plugin-integration/translate_documentation.gif)

> [!warning]
>
> 无法保证翻译后能够正确地识别并保留各种特殊标记。

### 意图：复制翻译后的本地化到剪贴板

当光标选取范围涉及到本地化时，此意图可以将所有涉及到的本地化复制到系统剪贴板。
本地化文本将被翻译为选中的语言区域。

![](../images/plugin-integration/intention_copy_loc_for_locale.gif)

> [!warning]
>
> 无法保证翻译后能够正确地识别并保留各种特殊标记。
