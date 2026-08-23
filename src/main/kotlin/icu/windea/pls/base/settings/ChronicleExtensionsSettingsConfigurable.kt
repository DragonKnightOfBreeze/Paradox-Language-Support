package icu.windea.pls.base.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.base.ChronicleBaseBundle
import icu.windea.pls.base.help.ChronicleHelpTopics
import icu.windea.pls.model.constants.ChronicleUrls

class ChronicleExtensionsSettingsConfigurable : BoundConfigurable(ChronicleBaseBundle.message("settings")), SearchableConfigurable {
    // private val callbackLock = CallbackLock()

    override fun getId() = "chronicle.extensions"

    override fun getHelpTopic() = ChronicleHelpTopics.extensionsSettings

    override fun createPanel(): DialogPanel {
        // callbackLock.reset()
        return panel {
            // markdown
            group(ChronicleBaseBundle.message("settings.extensions.markdown")) { configureGroupForMarkdown() }
        }
    }

    private fun Panel.configureGroupForMarkdown() {
        val settings = ChronicleExtensionsSettings.getInstance().state.markdown

        // resolveLinks
        row {
            checkBox(ChronicleBaseBundle.message("settings.extensions.markdown.resolveLinks")).bindSelected(settings::resolveLinks)
            contextHelp(ChronicleBaseBundle.message("settings.extensions.markdown.resolveLinks.tip"))
            browserLink(ChronicleBundle.message("link.documentation"), ChronicleUrls.refDoc("extensions.html#md-link"))
        }
        // resolveInlineCodes
        row {
            checkBox(ChronicleBaseBundle.message("settings.extensions.markdown.resolveInlineCodes")).bindSelected(settings::resolveInlineCodes)
            contextHelp(ChronicleBaseBundle.message("settings.extensions.markdown.resolveInlineCodes.tip"))
            browserLink(ChronicleBundle.message("link.documentation"), ChronicleUrls.refDoc("extensions.html#md-inline-code"))
        }
        // injectCodeBlocks
        row {
            checkBox(ChronicleBaseBundle.message("settings.extensions.markdown.injectCodeBlocks")).bindSelected(settings::injectCodeBlocks)
            contextHelp(ChronicleBaseBundle.message("settings.extensions.markdown.injectCodeBlocks.tip"))
            browserLink(ChronicleBundle.message("link.documentation"), ChronicleUrls.refDoc("extensions.html#md-code-block"))
        }
    }
}
