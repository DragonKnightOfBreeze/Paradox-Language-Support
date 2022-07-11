# 插件集成

## 集成**Translation**插件

当同时安装且启用了[Translation](https://github.com/YiiGuxing/TranslationPlugin)插件时，PLS将会额外提供以下功能：

### 翻译文档内容

当光标位于定义的名字、本地化的键以及CWT规则表达式上时，可以通过点击右键菜单中的`Translate Documentation`翻译文档内容。



### 意向：复制本地化到剪贴板并将本地化文本翻译为指定语言区域

当光标选取范围涉及到本地化时，此意向可以将所有涉及到的本地化复制到剪切板，并且在这之前尝试将本地化文本翻译到指定的语言区域。

![](../assets/images/plugin-integration/intention_copy_loc_for_locale.gif)

**注意：**目前无法保证翻译后能够正确地保留各种特殊标记（如彩色文本标记）。