package icu.windea.pls

import com.intellij.openapi.help.WebHelpProvider
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.constants.PlsHelpTopics

/**
 * 转到插件的参考文档。
 */
class PlsHelpProvider : WebHelpProvider() {
    override fun getHelpPageUrl(helpTopicId: String): String? {
        return when (helpTopicId) {
            PlsHelpTopics.configSettings -> PlsConstants.docUrl("config.html#settings-page")
            PlsHelpTopics.integrationsSettings -> PlsConstants.docUrl("integrations.html#settings-page")
            PlsHelpTopics.aiSettings -> PlsConstants.docUrl("ai.html#settings-page")
            PlsHelpTopics.extensionsSettings -> PlsConstants.docUrl("extensions.html#settings-page")
            PlsHelpTopics.diagramSettings -> PlsConstants.docUrl("extensions.html#diagram-settings-page")
            PlsHelpTopics.diagramEventTree -> PlsConstants.docUrl("extensions.html#diagram-event-tree")
            PlsHelpTopics.diagramTechTree -> PlsConstants.docUrl("extensions.html#diagram-tech-tree")
            else -> null
        }
    }
}
