# 集成

PLS 集成了一些额外的工具，可以用来优化或者扩展插件的功能。

通常情况下，PLS 不需要启用与这些工具的集成就能正常工作，并且提供足够丰富的功能。
但是，在一些特定情况下，建议启用与这些工具的集成，以修复插件自身无法解决的一些问题，并且获得更好的使用体验。

## 设置页面 {#settings-page}

在 IDE 的设置页面中，点击 `Languages & Frameworks > Paradox Language Support > 集成`，可以打开集成的设置页面。

可以在这里按类型配置要启用与哪些工具的集成，以及工具的具体设置，例如可执行文件的路经。

![](../images/integrations/integrations_settings_1.png)

## 图片工具

PLS 可以使用以下工具处理图片，特别是对于 PNG、DDS 和 TGA 图片。优先级由低到高。

- 基于 [Texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv)
  - 内置，仅适用于 Windows。
- 基于 [Image Magick](https://www.imagemagick.org)
  - 适用于 Window 和 Linux。
  - 需要在设置页面中指定正确的可执行文件的路经。

默认的，PLS 使用 [TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys) 处理图片。
这是一个Java 图片处理库，它为 `ImageIO` 适配了多种额外的图片类型。借助它，PLS 可以如同渲染普通的 PNG 图片一样渲染 DDS 和 TGA 图片。
你也可以为 PLS 指定图片工具，此时，如果需要渲染 DDS 和TGA 图片，插件会先执行图片工具，将其转换为临时 PNG 图片，然后再读取到 `ImageIO` 中。

这适用于很多场合，最常见的是直接在 IDE 中打开图片以进行预览（编辑图片仍然需要在外部编辑器中进行），以及在快速文档与内嵌提示中渲染图片。
你也可以直接在 HTML 文档与 Markdown 文档中引用 DDS 图片，尽管一般来说不能这么做，但是你可以直接在 IDE 中预览渲染后的文档，此时它们应当能够正常显示。 

某些场景下，PLS 需要切分图片以更准确地渲染快速文档与内嵌提示，生成的 PNG 图片可以在 `~/.pls/images` 中找到。

此外，PLS 也提供不同图片格式（PNG、DDS、TGA）之间的相互转换的操作。
插件也允许直接在项目视图中选择多个图片，然后执行图片类型的批量转换。

![](../images/integrations/convert_image_format_1.png)

![](../images/integrations/convert_image_format_2.png)<!--batch-->

> [!WARNING]
> 
> 请注意， [TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys) 对 DDS 图片的支持存在一些限制，例如，它不支持 DX11 格式。
> 
> 如果你遇到 PLS 无法正常渲染 DDS 图片的问题，我们推荐启用插件与 [Image Magick](https://www.imagemagick.org) 的集成。
> 从官网下载程序包到本地，在设置页面中启用集成，正确配置其可执行文件（名为`magick.exe`或者`magick`）的路经，然后保存设置。
> 重启 IDE 后，问题应当能够得到解决。

> [!TIP]
> 
> 如果你需要可视化地编辑 DDS 和 TGA 图片，我们推荐使用 [Paint.NET](https://www.getpaint.net)。
> 
> 如果你需要批处理 DDS 和 TGA 图片，例如批量调整图片尺寸，我们推荐使用 [Image Magick](https://www.imagemagick.org)。

> [!TIP]
> 
> 你可以通过以下几种方式，直接从 IDE 转到外部的图片工具。
> 
> 1. 通过 Intellij IDEA 的 [外部工具](https://www.jetbrains.com/help/idea/configuring-third-party-tools.html) 功能
>   - 在 IDE 的设置页面中，点击 `Tools > External Tools`，然后配置好外部工具。
>   - 在编辑器中打开目标文件（或者在项目视图中选中目标文件），然后在右键菜单中，点击 `External Tools > {toolName}`。
> 2. 直接转到图片的外部编辑器
>   - 在操作系统上，配置好图片的打开方式。
>   - 在编辑器中打开目标文件（或者在项目视图中选中目标文件），然后在右键菜单中，点击 `Jump to External Editor`。
> 
> ![](../images/integrations/jump_to_image_editor_1.png)
> 
> ![](../images/integrations/jump_to_image_editor_2.png)

## 翻译工具

PLS 可以使用以下翻译工具来翻译文本，特别是对于本地化文本。

- 基于 [Translation 插件](https://github.com/yiiguxing/TranslationPlugin)
  - 如果启用了此插件，PLS 可以提供额外的功能，例如意图和操作。
- 基于 AI
  - 需要在 AI 设置页面中配置，请参考[这里](ai.md#settings_page)

如果翻译工具可用，PLS 将会提供额外的用于操作本地化的意图和操作。
它们可以用来批量地翻译本地化文本，然后复制到系统剪贴板，或是进行流式替换。

你可以从多个入口（工具菜单，编辑器右键菜单，项目视图的右键菜单，等等）， 在多个批处理级别（光标位置对应的本地化，光标选取范围涉及到的所有本地化，或是项目视图中选中的文件和目录中的所有本地化） 访问这些功能。

![](../images/integrations/translation_entry_intentions_1.png)

![](../images/integrations/translation_entry_actions_1.png)

![](../images/integrations/translation_entry_actions_2.png)<!--batch-->

以下是一个使用 AI 批量翻译本地化文本的演示：

<ArtPlayer src="/videos/integrations/translate_and_replace_1.mp4" poster="../images/translate_and_replace_1.png" />

## 检查工具

PLS 可以通过以下检查工具提供额外的代码检查。

- [Tiger](https://github.com/amtep/tiger)
  - 适用于 Window 和 Linux。
  - 仅适用于模组目录。
  - 支持的游戏类型：`ck3`, `ir`, `vic3`。
  - 需要在设置页面中指定正确的可执行文件的路经。
  - 可以在设置页面中指定使用的配置文件的路经。
  - 模组目录下的正确位置的配置文件会被自动应用。

如果启用并正确配置了与这些工具的集成，PLS 可以提供额外的代码检查。
你可以直接在问题视图中查看检查结果，或是执行全局代码检查并查看总览。

![](../images/integrations/lint_results_1.png)

![](../images/integrations/lint_results_2.png)<!--batch-->

> [!TIP]
> 
> 你可以通过以下几种方式，在项目级别或者模组级别选择性禁用检查工具。
> - 项目级别：在 IDE 的设置页面中，点击 `Editor > Inspections`，搜索`paradox lint`，然后按需禁用搜索到的检查项。
> - 模组级别：在模组设置对话框中，展开 `额外选项`，然后按需禁用。