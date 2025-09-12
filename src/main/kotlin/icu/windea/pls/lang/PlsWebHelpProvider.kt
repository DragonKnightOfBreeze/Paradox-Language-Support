package icu.windea.pls.lang

import com.intellij.openapi.help.WebHelpProvider
import icu.windea.pls.model.constants.PlsConstants

/**
 * 转到插件的参考文档。
 */
class PlsWebHelpProvider : WebHelpProvider() {
    private val prefix = "${PlsConstants.pluginId}."
    private val docBaseUrl = "https://windea.icu/Paradox-Language-Support"

    override fun getHelpPageUrl(helpTopicId: String): String? {
        val shortId = helpTopicId.removePrefix(prefix)
        return when (shortId) {
            // "config.settings" -> "$docBaseUrl/config.html#settings-page" // TODO 2.0.5+
            "integrations.settings" -> "$docBaseUrl/integrations.html#settings-page"
            "ai.settings" -> "$docBaseUrl/ai.html#settings-page"
            "diagram.settings" -> "$docBaseUrl/extensions.html#diagram-settings-page"
            // "diagram.eventTree" -> "$docBaseUrl/extensions.html#event-tree-diagram"
            // "diagram.techTree" -> "$docBaseUrl/extensions.html#tech-tree-diagram"
            else -> null
        }
    }
}
