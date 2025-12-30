package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.declarative.InlayHintsCustomSettingsProvider
import com.intellij.lang.Language
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import javax.swing.JComponent

/**
 * @see ParadoxDefinitionReferenceHintTextHintsProvider
 */
class ParadoxDefinitionReferenceInfoSettingsProvider : InlayHintsCustomSettingsProvider<Boolean> {
    private val showDefinitionSubtypesProperty = AtomicProperty(true)
    private var showDefinitionSubtypes by showDefinitionSubtypesProperty

    private val component by lazy {
        panel {
            row {
                checkBox(PlsBundle.message("hints.settings.showDefinitionTypes")).selected(true).enabled(false)
                checkBox(PlsBundle.message("hints.settings.showDefinitionSubtypes")).bindSelected(showDefinitionSubtypesProperty)
            }
        }.also { it.border = JBUI.Borders.empty(5) }
    }

    override fun createComponent(project: Project, language: Language): JComponent {
        showDefinitionSubtypes = getSettingsInstance(project).showSubtypesForCsvDefinitionReference
        return component
    }

    override fun isDifferentFrom(project: Project, settings: Boolean): Boolean {
        return settings != showDefinitionSubtypes
    }

    override fun getSettingsCopy(): Boolean {
        return showDefinitionSubtypes
    }

    override fun putSettings(project: Project, settings: Boolean, language: Language) {
        showDefinitionSubtypes = settings
    }

    override fun persistSettings(project: Project, settings: Boolean, language: Language) {
        getSettingsInstance(project).showSubtypesForCsvDefinitionReference = settings
    }

    private fun getSettingsInstance(project: Project): ParadoxDeclarativeHintsSettings {
        return ParadoxDeclarativeHintsSettings.getInstance(project)
    }
}
