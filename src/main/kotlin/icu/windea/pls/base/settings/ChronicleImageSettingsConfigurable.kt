package icu.windea.pls.base.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.base.ChronicleBaseBundle
import icu.windea.pls.model.policies.ImageCacheCleanupPolicy
import icu.windea.pls.model.policies.ImageRenderFailurePolicy

class ChronicleImageSettingsConfigurable : BoundConfigurable(ChronicleBaseBundle.message("settings.images")), SearchableConfigurable {
    override fun getId() = "chronicle.images"

    override fun getHelpTopic() = null

    override fun createPanel(): DialogPanel {
        return panel {
            group(ChronicleBaseBundle.message("settings.images.cache")) { configureGroupForCache() }
            group(ChronicleBaseBundle.message("settings.images.render")) { configureGroupForRender() }
        }
    }

    private fun Panel.configureGroupForCache() {
        val group = "chronicle.images.cache"
        val settings = ChronicleImageSettings.getInstance().state

        // cacheCleanupPolicy
        row {
            label(ChronicleBaseBundle.message("settings.images.cache.cleanupPolicy")).widthGroup(group)
            comboBox(ImageCacheCleanupPolicy.entries, textListCellRenderer { it?.text })
                .bindItem(settings::cacheCleanupPolicy.toNullableProperty())
            contextHelp(ChronicleBaseBundle.message("settings.images.cache.cleanupPolicy.tip"))
        }
        // cacheTtlDays
        row {
            label(ChronicleBaseBundle.message("settings.images.cache.ttlDays")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::cacheTtlDays)
            contextHelp(ChronicleBaseBundle.message("settings.images.cache.ttlDays.tip"))
        }
        // cacheMaxSizeMb
        row {
            label(ChronicleBaseBundle.message("settings.images.cache.maxSizeMb")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::cacheMaxSizeMb)
            contextHelp(ChronicleBaseBundle.message("settings.images.cache.maxSizeMb.tip"))
        }
        // cacheMaxFiles
        row {
            label(ChronicleBaseBundle.message("settings.images.cache.maxFiles")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::cacheMaxFiles)
            contextHelp(ChronicleBaseBundle.message("settings.images.cache.maxFiles.tip"))
        }
    }

    private fun Panel.configureGroupForRender() {
        val group = "chronicle.images.render"
        val settings = ChronicleImageSettings.getInstance().state

        // renderTimeoutSeconds
        row {
            label(ChronicleBaseBundle.message("settings.images.render.timeoutSeconds")).widthGroup(group)
            intTextField(1..Int.MAX_VALUE, 1).bindIntText(settings::renderTimeoutSeconds)
            contextHelp(ChronicleBaseBundle.message("settings.images.render.timeoutSeconds.tip"))
        }
        // renderFailurePolicy
        row {
            label(ChronicleBaseBundle.message("settings.images.render.failurePolicy")).widthGroup(group)
            comboBox(ImageRenderFailurePolicy.entries, textListCellRenderer { it?.text })
                .bindItem(settings::renderFailurePolicy.toNullableProperty())
            contextHelp(ChronicleBaseBundle.message("settings.images.render.failurePolicy.tip"))
        }
    }
}
