package icu.windea.pls.extensions.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.extensions.ChronicleExtensionsBundle
import icu.windea.pls.ide.help.ChronicleHelpTopics
import icu.windea.pls.model.constants.ChronicleConstants

class PlsExtensionsSettingsConfigurable : BoundConfigurable(ChronicleExtensionsBundle.message("settings")), SearchableConfigurable {
    // private val callbackLock = CallbackLock()

    override fun getId() = "pls.extensions"

    override fun getHelpTopic() = ChronicleHelpTopics.extensionsSettings

    override fun createPanel(): DialogPanel {
        // callbackLock.reset()
        return panel {
            // markdown
            group(ChronicleExtensionsBundle.message("settings.markdown")) { configureGroupForMarkdown() }
        }
    }

    private fun Panel.configureGroupForMarkdown() {
        val settings = PlsExtensionsSettings.getInstance().state.markdown

        // resolveLinks
        row {
            checkBox(ChronicleExtensionsBundle.message("settings.markdown.resolveLinks")).bindSelected(settings::resolveLinks)
            contextHelp(ChronicleExtensionsBundle.message("settings.markdown.resolveLinks.tip"))
            browserLink(ChronicleBundle.message("link.documentation"), ChronicleConstants.docUrl("extensions.html#md-link"))
        }
        // resolveInlineCodes
        row {
            checkBox(ChronicleExtensionsBundle.message("settings.markdown.resolveInlineCodes")).bindSelected(settings::resolveInlineCodes)
            contextHelp(ChronicleExtensionsBundle.message("settings.markdown.resolveInlineCodes.tip"))
            browserLink(ChronicleBundle.message("link.documentation"), ChronicleConstants.docUrl("extensions.html#md-inline-code"))
        }
        // injectCodeBlocks
        row {
            checkBox(ChronicleExtensionsBundle.message("settings.markdown.injectCodeBlocks")).bindSelected(settings::injectCodeBlocks)
            contextHelp(ChronicleExtensionsBundle.message("settings.markdown.injectCodeBlocks.tip"))
            browserLink(ChronicleBundle.message("link.documentation"), ChronicleConstants.docUrl("extensions.html#md-code-block"))
        }
    }
}
