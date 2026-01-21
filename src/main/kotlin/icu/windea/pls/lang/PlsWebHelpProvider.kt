package icu.windea.pls.lang

import com.intellij.openapi.help.WebHelpProvider
import icu.windea.pls.model.constants.PlsConstants

/**
 * 转到插件的参考文档。
 */
class PlsWebHelpProvider : WebHelpProvider() {
    private val prefix = "${PlsConstants.pluginId}."

    override fun getHelpPageUrl(helpTopicId: String): String? {
        val shortId = helpTopicId.removePrefix(prefix)
        return when (shortId) {
            "config.settings" -> PlsConstants.docUrl("config.html#settings-page")
            "integrations.settings" -> PlsConstants.docUrl("integrations.html#settings-page")
            "ai.settings" -> PlsConstants.docUrl("ai.html#settings-page")
            "diagram.settings" -> PlsConstants.docUrl("extensions.html#diagram-settings-page")
            // "diagram.eventTree" -> PlsConstants.docUrl("extensions.html#event-tree-diagram")
            // "diagram.techTree" -> PlsConstants.docUrl("extensions.html#tech-tree-diagram")
            else -> null
        }
    }
}
