package icu.windea.pls.ide.help

import com.intellij.openapi.help.WebHelpProvider
import icu.windea.pls.model.constants.ChronicleUrls

/**
 * 转到插件的参考文档。
 */
class ChronicleHelpProvider : WebHelpProvider() {
    override fun getHelpPageUrl(helpTopicId: String): String? {
        return when (helpTopicId) {
            ChronicleHelpTopics.configSettings -> ChronicleUrls.refDoc("config.html#settings-page")
            ChronicleHelpTopics.integrationsSettings -> ChronicleUrls.refDoc("integrations.html#settings-page")
            ChronicleHelpTopics.aiSettings -> ChronicleUrls.refDoc("ai.html#settings-page")
            ChronicleHelpTopics.extensionsSettings -> ChronicleUrls.refDoc("extensions.html#settings-page")
            ChronicleHelpTopics.diagramSettings -> ChronicleUrls.refDoc("extensions.html#diagram-settings-page")
            ChronicleHelpTopics.diagramEventTree -> ChronicleUrls.refDoc("extensions.html#diagram-event-tree")
            ChronicleHelpTopics.diagramTechTree -> ChronicleUrls.refDoc("extensions.html#diagram-tech-tree")
            else -> null
        }
    }
}
