package icu.windea.pls.extensions.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import icu.windea.pls.model.constants.PlsConstants

/**
 * PLS 扩展设置。可以在插件的对应设置页面中进行配置。
 */
@Service
@State(name = "PlsExtensionsSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsExtensionsSettings : SimplePersistentStateComponent<PlsExtensionsSettings.State>(State()) {
    companion object {
        @JvmStatic
        fun getInstance(): PlsExtensionsSettings = service()
    }

    /**
     * @property markdown Markdown 的设置。
     */
    class State : BaseState() {
        var markdown by property(MarkdownState())
    }

    /**
     * @property resolveLinks 是否尝试将 Markdown 链接解析为匹配的目标引用（定义、本地化等）。
     * @property resolveInlineCodes 是否尝试将 Markdown 内联代码解析为匹配的目标引用（定义、本地化等）。
     * @property injectCodeBlocks 是否为 Markdown 代码块应用注入（基于语言 ID 后面的额外信息）。
     */
    class MarkdownState : BaseState() {
        var resolveLinks: Boolean by property(true)
        var resolveInlineCodes: Boolean by property(true) // NOTE 2.1.2 enabled by default now
        var injectCodeBlocks: Boolean by property(true)
    }
}
