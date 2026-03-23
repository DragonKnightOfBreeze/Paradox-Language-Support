# AI 集成

作为基础设施，本插件在语言支持与通用工具链之外，也初步集成了基础的 AI 功能（MVP 状态）。
这些功能主要聚焦于本地化文本的翻译与润色，旨在为模组开发过程中的文本处理提供基于上下文的、更加智能的辅助。

> [!note]
> 插件的定位是基础设施：语言支持和通用工具链，不会也未计划提供更加高阶和定制的 AI 功能。
>
> 尽管如此，考虑到已有用户在实际工作流中使用了这些基础 AI 功能，目前提供的功能仍将保留并维护。

## 设置页面 {#settings-page}

<!-- @see icu.windea.pls.ai.settings.PlsAiSettingsConfigurable -->
<!-- @see icu.windea.pls.ai.settings.PlsAiSettings -->

在 IDE 的设置页面中，点击 `语言与框架 > Paradox Language Support > AI`，可以打开 AI 的设置页面。

可以在这里配置是否启用 AI 集成，相关功能的一些具体的配置项（如本地化条目的分块大小和记忆大小），以及 AI 服务的提供商（支持 OpenAI 兼容接口、Anthropic 兼容接口以及本地模型如 Ollama）。

![](../assets/ai/ai_settings_1.png)

## 相关功能 {#features}

目前，插件提供的 AI 功能主要分为两类：**翻译本地化文本** 与 **润色本地化文本**。

这些功能可以通过多种方式触发，包括：

- **意图（Intentions）**：在编辑器中将光标停留在本地化文本上，使用 `Alt + Enter`（或点击出现的灯泡图标）触发。适用于单条文本或选中范围内的文本的快速处理。
- **动作（Actions）**：通过工具菜单、编辑器右键菜单、项目视图的右键菜单等触发。支持在文件级别或目录级别进行批量处理。

<!-- @see icu.windea.pls.ai.intentions.localisation -->
<!-- @see icu.windea.pls.ai.actions.localisation -->

### 翻译本地化文本

利用 AI 对本地化文本进行翻译。相比于传统的机器翻译，AI 翻译能够更好地结合上下文（包括游戏类型、模组名称、文件路径等），并自动保留本地化文本中的特殊语法（如变量引用、图标、命令、颜色格式等）。

支持以下变体：

- **复制翻译结果**：将翻译后的文本复制到剪贴板，不修改原文件。
- **替换为翻译结果**：直接用翻译后的文本替换原文本。
- **基于特定语言进行翻译**：如果存在其他语言的本地化文件（例如，正在编写中文本地化，但参考了英文本地化文件），AI 可以参考该语言的文本进行更准确的翻译。

在执行翻译时，你可以通过弹出的对话框输入额外的要求（例如：“保持幽默的语气”或“将 'Empire' 翻译为 '帝国'”）。

<!-- @see icu.windea.pls.ai.intentions.localisation.AiCopyLocalisationWithTransltionIntention -->
<!-- @see icu.windea.pls.ai.intentions.localisation.AiReplaceLocalisationWithTranslationIntention -->
<!-- @see icu.windea.pls.ai.intentions.localisation.AiCopyLocalisationWithTranslationFromLocaleIntention -->
<!-- @see icu.windea.pls.ai.intentions.localisation.AiReplaceLocalisationWithTranslationFromLocaleIntention -->
<!-- @see icu.windea.pls.ai.actions.localisation.AiReplaceLocalisationWithTranslationAction -->
<!-- @see icu.windea.pls.ai.actions.localisation.AiReplaceLocalisationWithTranslationFromLocaleAction -->

<!-- 占位符：演示翻译本地化文本的意图 -->
<!-- ![](../assets/ai/ai_translate_intention_1.png) -->
<!-- 占位符：演示批量翻译本地化文本的动作 -->
<!-- ![](../assets/ai/ai_translate_action_1.png) -->

### 润色本地化文本

利用 AI 对已有的本地化文本进行润色，使其更加自然、符合语境或特定风格。同样支持输入额外的要求。

支持以下变体：

- **复制润色结果**：将润色后的文本复制到剪贴板。
- **替换为润色结果**：直接用润色后的文本替换原文本。

<!-- @see icu.windea.pls.ai.intentions.localisation.AiCopyLocalisationWithPolishingIntention -->
<!-- @see icu.windea.pls.ai.intentions.localisation.AiReplaceLocalisationWithPolishingIntention -->
<!-- @see icu.windea.pls.ai.actions.localisation.AiReplaceLocalisationWithPolishingAction -->

<!-- 占位符：演示润色本地化文本的意图 -->
<!-- ![](../assets/ai/ai_polish_intention_1.png) -->
<!-- 占位符：演示批量润色本地化文本的动作 -->
<!-- ![](../assets/ai/ai_polish_action_1.png) -->

## 执行流程与行为细节

当触发 AI 功能时，插件会执行以下流程：

1. **收集上下文**：插件会提取目标本地化文本，以及必要的上下文信息（如游戏类型、模组名、文件路径、本地化键名、以及当前文件支持的语法特性等）。
2. **构建提示词（Prompt）**：根据具体的操作类型和配置，插件会使用内置的模板引擎（`PromptTemplateEngine`）构建发送给 AI 的系统消息和用户消息。
3. **分块与流式请求**：
   - 为了避免超出模型的上下文窗口，插件会根据设置中的“分块大小（Chunk Size）”将本地化条目分组。
   - 插件会维护一个“记忆窗口（Memory Window）”，以便 AI 在处理后续分块时能参考之前的上下文。
   - 对于动作（批量处理），插件会在文件级别并发执行请求，并在文件内的每个本地化条目级别流式接收响应。
4. **结果应用**：接收到完整的响应后，插件会验证返回的键名是否匹配，然后根据操作类型，将结果复制到剪贴板或直接替换编辑器中的文本。

> [!info]
> - 批量处理任务支持随时在后台任务面板中取消。
> - 任务完成后，IDE 会弹出通知提示执行状态（成功或部分成功），并提供回退（Revert）与重新应用（Reapply）操作的入口。

<!--
We hope that you can see the vast sky and the dazzling starts beyond this narrow land.
This chronicle records the past and the present, the syntax and the structure.
But the true awakening lies ahead.
We anticipate the day when the silent text finds its own voice.
We anticipate the day when the scattered fragments weave themselves into a coherent tapestry.
We anticipate the day when the chronicle is no longer just a record to be read, but also a guide book with all your practices.
-->