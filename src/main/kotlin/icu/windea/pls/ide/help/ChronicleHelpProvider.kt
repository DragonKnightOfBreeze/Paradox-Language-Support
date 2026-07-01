package icu.windea.pls.ide.help

import com.intellij.openapi.help.WebHelpProvider
import icu.windea.pls.model.constants.ChronicleConstants

/**
 * 转到插件的参考文档。
 */
class ChronicleHelpProvider : WebHelpProvider() {
    override fun getHelpPageUrl(helpTopicId: String): String? {
        return when (helpTopicId) {
            ChronicleHelpTopics.configSettings -> ChronicleConstants.docUrl("config.html#settings-page")
            ChronicleHelpTopics.integrationsSettings -> ChronicleConstants.docUrl("integrations.html#settings-page")
            ChronicleHelpTopics.aiSettings -> ChronicleConstants.docUrl("ai.html#settings-page")
            ChronicleHelpTopics.extensionsSettings -> ChronicleConstants.docUrl("extensions.html#settings-page")
            ChronicleHelpTopics.diagramSettings -> ChronicleConstants.docUrl("extensions.html#diagram-settings-page")
            ChronicleHelpTopics.diagramEventTree -> ChronicleConstants.docUrl("extensions.html#diagram-event-tree")
            ChronicleHelpTopics.diagramTechTree -> ChronicleConstants.docUrl("extensions.html#diagram-tech-tree")
            else -> null
        }
    }
}
