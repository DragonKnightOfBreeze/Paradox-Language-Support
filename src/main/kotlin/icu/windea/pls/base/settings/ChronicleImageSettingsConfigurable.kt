package icu.windea.pls.base.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.base.ChronicleBaseBundle
import icu.windea.pls.model.policies.ImageCacheCleanupPolicy
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import icu.windea.pls.model.policies.ImageRenderPolicy

class ChronicleImageSettingsConfigurable : BoundConfigurable(ChronicleBaseBundle.message("settings.image")), SearchableConfigurable {
    override fun getId() = "chronicle.image"

    override fun getHelpTopic() = null

    override fun createPanel(): DialogPanel {
        return panel {
            group(ChronicleBaseBundle.message("settings.image.cache")) { configureGroupForCache() }
            group(ChronicleBaseBundle.message("settings.image.render")) { configureGroupForRender() }
        }
    }

    private fun Panel.configureGroupForCache() {
        val group = "chronicle.image.cache"
        val settings = ChronicleImageSettings.getInstance().state

        // cacheCleanupPolicy
        row {
            label(ChronicleBaseBundle.message("settings.image.cache.cleanupPolicy")).widthGroup(group)
            comboBox(ImageCacheCleanupPolicy.entries, textListCellRenderer { it?.text })
                .bindItem(settings::cacheCleanupPolicy.toNullableProperty())
            contextHelp(ChronicleBaseBundle.message("settings.image.cache.cleanupPolicy.tip"))
        }
        // cacheTtlDays
        row {
            label(ChronicleBaseBundle.message("settings.image.cache.ttlDays")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::cacheTtlDays)
            contextHelp(ChronicleBaseBundle.message("settings.image.cache.ttlDays.tip"))
        }
        // cacheMaxSizeMb
        row {
            label(ChronicleBaseBundle.message("settings.image.cache.maxSizeMb")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::cacheMaxSizeMb)
            contextHelp(ChronicleBaseBundle.message("settings.image.cache.maxSizeMb.tip"))
        }
        // cacheMaxFiles
        row {
            label(ChronicleBaseBundle.message("settings.image.cache.maxFiles")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::cacheMaxFiles)
            contextHelp(ChronicleBaseBundle.message("settings.image.cache.maxFiles.tip"))
        }
    }

    private fun Panel.configureGroupForRender() {
        val group = "chronicle.image.render"
        val settings = ChronicleImageSettings.getInstance().state

        // renderPolicy
        row {
            label(ChronicleBaseBundle.message("settings.image.render.policy")).widthGroup(group)
            comboBox(ImageRenderPolicy.entries, textListCellRenderer { it?.text })
                .bindItem(settings::renderPolicy.toNullableProperty())
            contextHelp(ChronicleBaseBundle.message("settings.image.render.policy.tip"))
        }
        // renderFailurePolicy
        row {
            label(ChronicleBaseBundle.message("settings.image.render.failurePolicy")).widthGroup(group)
            comboBox(ImageRenderFailurePolicy.entries, textListCellRenderer { it?.text })
                .bindItem(settings::renderFailurePolicy.toNullableProperty())
            contextHelp(ChronicleBaseBundle.message("settings.image.render.failurePolicy.tip"))
        }
    }
}

typealias ChronicleImageSettingsConfigure = ChronicleImageSettingsConfigurable


