package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayHintsCustomSettingsProvider
import com.intellij.lang.Language
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.smaller
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxDefinitionReferenceInfoSettingsProvider.*
import javax.swing.JComponent

// com.intellij.codeInsight.hints.chain.AbstractDeclarativeCallChainCustomSettingsProvider

/**
 * @see ParadoxDefinitionReferenceInfoHintsProvider
 */
class ParadoxDefinitionReferenceInfoSettingsProvider : InlayHintsCustomSettingsProvider<Settings> {
    data class Settings(
        val showSubtypes: Boolean,
        val truncateSubtypes: Int,
    )

    private val showSubtypesProperty = AtomicProperty(true)
    private val truncateSubtypesProperty = AtomicProperty(-1)
    private var showSubtypes by showSubtypesProperty
    private var truncateSubtypes by truncateSubtypesProperty

    private val component by lazy {
        panel {
            row {
                checkBox(PlsBundle.message("hints.settings.showDefinitionType")).selected(true).enabled(false).smaller()
            }
            row {
                checkBox(PlsBundle.message("hints.settings.showDefinitionSubtypes")).bindSelected(showSubtypesProperty).smaller()
            }
            row(PlsBundle.message("hints.settings.truncateDefinitionSubtypes")) {
                intTextField().bindIntText(truncateSubtypesProperty).smaller()
            }
        }.also { it.border = JBUI.Borders.empty(5) }
    }

    override fun createComponent(project: Project, language: Language): JComponent {
        val s = ParadoxDeclarativeHintsSettings.getInstance(project)
        showSubtypes = s.showDefinitionSubtypesForReferences
        truncateSubtypes = s.truncateDefinitionSubtypesForReferences
        return component
    }

    override fun isDifferentFrom(project: Project, settings: Settings): Boolean {
        return settings.showSubtypes != showSubtypes
            && settings.truncateSubtypes != truncateSubtypes
    }

    override fun getSettingsCopy(): Settings {
        return Settings(showSubtypes, truncateSubtypes)
    }

    override fun putSettings(project: Project, settings: Settings, language: Language) {
        showSubtypes = settings.showSubtypes
        truncateSubtypes = settings.truncateSubtypes
    }

    override fun persistSettings(project: Project, settings: Settings, language: Language) {
        val s = ParadoxDeclarativeHintsSettings.getInstance(project)
        s.showDefinitionSubtypesForReferences = settings.showSubtypes
        s.truncateDefinitionSubtypesForReferences = settings.truncateSubtypes
    }
}
