package icu.windea.pls.extensions.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.extensions.PlsExtensionsBundle
import icu.windea.pls.ide.help.PlsHelpTopics
import icu.windea.pls.model.constants.PlsConstants

class PlsExtensionsSettingsConfigurable : BoundConfigurable(PlsExtensionsBundle.message("settings")), SearchableConfigurable {
    // private val callbackLock = CallbackLock()

    override fun getId() = "pls.extensions"

    override fun getHelpTopic() = PlsHelpTopics.extensionsSettings

    override fun createPanel(): DialogPanel {
        // callbackLock.reset()
        return panel {
            // markdown
            group(PlsExtensionsBundle.message("settings.markdown")) { configureGroupForMarkdown() }
        }
    }

    private fun Panel.configureGroupForMarkdown() {
        val settings = PlsExtensionsSettings.getInstance().state.markdown

        // resolveLinks
        row {
            checkBox(PlsExtensionsBundle.message("settings.markdown.resolveLinks")).bindSelected(settings::resolveLinks)
            contextHelp(PlsExtensionsBundle.message("settings.markdown.resolveLinks.tip"))
            browserLink(PlsBundle.message("link.documentation"), PlsConstants.docUrl("extensions.html#md-link"))
        }
        // resolveInlineCodes
        row {
            checkBox(PlsExtensionsBundle.message("settings.markdown.resolveInlineCodes")).bindSelected(settings::resolveInlineCodes)
            contextHelp(PlsExtensionsBundle.message("settings.markdown.resolveInlineCodes.tip"))
            browserLink(PlsBundle.message("link.documentation"), PlsConstants.docUrl("extensions.html#md-inline-code"))
        }
        // injectCodeBlocks
        row {
            checkBox(PlsExtensionsBundle.message("settings.markdown.injectCodeBlocks")).bindSelected(settings::injectCodeBlocks)
            contextHelp(PlsExtensionsBundle.message("settings.markdown.injectCodeBlocks.tip"))
            browserLink(PlsBundle.message("link.documentation"), PlsConstants.docUrl("extensions.html#md-code-block"))
        }
    }
}
