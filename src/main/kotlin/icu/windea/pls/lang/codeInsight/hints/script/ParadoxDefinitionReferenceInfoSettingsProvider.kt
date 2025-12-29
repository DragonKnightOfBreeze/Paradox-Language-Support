package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayHintsCustomSettingsProvider
import com.intellij.lang.Language
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import javax.swing.JComponent

// com.intellij.codeInsight.hints.chain.AbstractDeclarativeCallChainCustomSettingsProvider

/**
 * @see ParadoxDefinitionReferenceInfoHintsProvider
 */
class ParadoxDefinitionReferenceInfoSettingsProvider : InlayHintsCustomSettingsProvider<Boolean> {
    private val showSubtypesProperty = AtomicProperty(true)
    private var showSubtypes by showSubtypesProperty

    private val component by lazy {
        panel {
            row {
                checkBox(PlsBundle.message("hints.settings.showDefinitionTypes")).selected(true).enabled(false)
                checkBox(PlsBundle.message("hints.settings.showDefinitionSubtypes")).bindSelected(showSubtypesProperty)
            }
        }.also { it.border = JBUI.Borders.empty(5) }
    }

    override fun createComponent(project: Project, language: Language): JComponent {
        showSubtypes = getSettingsInstance(project).showSubtypesForDefinitionReference
        return component
    }

    override fun isDifferentFrom(project: Project, settings: Boolean): Boolean {
        return settings != showSubtypes
    }

    override fun getSettingsCopy(): Boolean {
        return showSubtypes
    }

    override fun putSettings(project: Project, settings: Boolean, language: Language) {
        showSubtypes = settings
    }

    override fun persistSettings(project: Project, settings: Boolean, language: Language) {
        getSettingsInstance(project).showSubtypesForDefinitionReference = settings
    }

    private fun getSettingsInstance(project: Project): ParadoxDeclarativeHintsSettings {
        return ParadoxDeclarativeHintsSettings.getInstance(project)
    }
}
